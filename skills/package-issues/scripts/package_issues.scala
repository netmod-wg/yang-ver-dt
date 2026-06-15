#!/usr/bin/env -S scala-cli
//> using scala "3.8.1"
//> using dep "com.lihaoyi::ujson:4.4.2"

import scala.sys.process.*
import scala.util.Try
import scala.util.boundary
import scala.util.boundary.break

object PackageIssues:
  private def escapeMd(value: String): String =
    value.replace("|", "\\|").replace("\n", " ").trim

  private def runGh(cmd: Seq[String]): Either[String, String] =
    Try(cmd.!!).toEither.left.map(_.getMessage)

  private def asString(v: ujson.Value): String = v match
    case ujson.Str(s) => s
    case ujson.Num(n) if n.isValidInt => n.toInt.toString
    case ujson.Num(n) => n.toString
    case ujson.Bool(b) => b.toString
    case ujson.Null => ""
    case other => other.toString

  private def dateOnly(value: String): String =
    value.split("T", 2).headOption.getOrElse("")

  def main(args: Array[String]): Unit =
    boundary:
      var repo: Option[String] = None
      var label = "packages"
      var limit = 200
      var issueNumber: Option[String] = None

      val it = args.iterator
      while it.hasNext do
        it.next() match
          case "--repo" if it.hasNext => repo = Some(it.next())
          case "--label" if it.hasNext => label = it.next()
          case "--limit" if it.hasNext =>
            limit = it.next().toIntOption.getOrElse(limit)
          case "--issue" if it.hasNext => issueNumber = Some(it.next())
          case unknown =>
            System.err.println(s"Unknown arg: $unknown")
            break()

      issueNumber match
        case Some(num) =>
          def parseRepo(value: String): Option[(String, String)] =
            value.split("/", 2) match
              case Array(owner, name) if owner.nonEmpty && name.nonEmpty =>
                Some(owner -> name)
              case _ => None

          val repoInfo: Option[(String, String)] =
            repo.flatMap(parseRepo).orElse {
              runGh(Seq("gh", "repo", "view", "--json", "nameWithOwner")) match
                case Left(err) =>
                  System.err.println(err)
                  None
                case Right(output) =>
                  val json = ujson.read(output)
                  json.objOpt
                    .flatMap(_.get("nameWithOwner"))
                    .map(asString)
                    .flatMap(parseRepo)
            }

          val (owner, name) = repoInfo.getOrElse {
            System.err.println("Unable to determine repository name.")
            break()
          }

          val query =
            "query($owner:String!,$name:String!,$number:Int!,$cursor:String){repository(owner:$owner,name:$name){issue(number:$number){number title body createdAt author{login} comments(first:100,after:$cursor){nodes{author{login} createdAt body} pageInfo{hasNextPage endCursor}}}}}"

          var cursor: Option[String] = None
          var issueTitle: Option[String] = None
          var issueNum: Option[String] = None
          var issueBody: Option[String] = None
          var issueCreated: Option[String] = None
          var issueAuthor: Option[String] = None
          var allComments = Vector.empty[ujson.Value]

          var keepGoing = true
          while keepGoing do
            val base = Seq(
              "gh",
              "api",
              "graphql",
              "-F",
              s"owner=$owner",
              "-F",
              s"name=$name",
              "-F",
              s"number=$num",
              "-f",
              s"query=$query",
            )
            val cmd = cursor match
              case Some(value) => base ++ Seq("-F", s"cursor=$value")
              case None => base

            runGh(cmd) match
              case Left(err) =>
                System.err.println(err)
                break()
              case Right(output) =>
                val json = ujson.read(output)
                val issue =
                  json.objOpt
                    .flatMap(_.get("data"))
                    .flatMap(_.objOpt)
                    .flatMap(_.get("repository"))
                    .flatMap(_.objOpt)
                    .flatMap(_.get("issue"))
                    .flatMap(_.objOpt)

                if issue.isEmpty then
                  System.err.println("Issue not found.")
                  break()

                if issueTitle.isEmpty then
                  issueTitle = issue.flatMap(_.get("title")).map(asString)
                  issueNum = issue.flatMap(_.get("number")).map(asString)
                  issueBody = issue.flatMap(_.get("body")).map(asString)
                  issueCreated = issue
                    .flatMap(_.get("createdAt"))
                    .map(asString)
                    .map(dateOnly)
                  issueAuthor = issue
                    .flatMap(_.get("author"))
                    .flatMap(_.objOpt)
                    .flatMap(_.get("login"))
                    .map(asString)

                val commentsObj = issue
                  .flatMap(_.get("comments"))
                  .flatMap(_.objOpt)
                val nodes = commentsObj
                  .flatMap(_.get("nodes"))
                  .flatMap(_.arrOpt)
                  .getOrElse(Seq.empty)
                allComments = allComments ++ nodes

                val pageInfo = commentsObj
                  .flatMap(_.get("pageInfo"))
                  .flatMap(_.objOpt)
                val hasNext = pageInfo
                  .flatMap(_.get("hasNextPage"))
                  .collect { case ujson.Bool(value) => value }
                  .getOrElse(false)
                if hasNext then
                  cursor = pageInfo
                    .flatMap(_.get("endCursor"))
                    .map(asString)
                else
                  keepGoing = false

          val numberOut = issueNum.getOrElse(num)
          val titleOut = issueTitle.getOrElse("")

          println(s"## Issue $numberOut: $titleOut")
          println("")

          val baseComments =
            issueBody.map { body =>
              val author = issueAuthor.getOrElse("unknown")
              val created = issueCreated.getOrElse("")
              (author, created, body, true)
            }.toVector

          val commentEntries = baseComments ++ allComments.map { comment =>
            val author = comment.objOpt
              .flatMap(_.get("author"))
              .flatMap(_.objOpt)
              .flatMap(_.get("login"))
              .map(asString)
              .getOrElse("unknown")
            val created = comment.objOpt
              .flatMap(_.get("createdAt"))
              .map(asString)
              .map(dateOnly)
              .getOrElse("")
            val body = comment.objOpt
              .flatMap(_.get("body"))
              .map(asString)
              .getOrElse("")
            (author, created, body, false)
          }

          if commentEntries.isEmpty then
            println("_No comments._")
            break()

          commentEntries.zipWithIndex.foreach { case ((author, created, body, isIssueBody), idx) =>
            val commentNumber = if baseComments.nonEmpty then idx else idx + 1
            val heading =
              if isIssueBody then "### Description"
              else s"### Comment $commentNumber"
            println(s"$heading ($author, $created)")
            println("")
            println(body)
            if idx + 1 < commentEntries.size then
              println("")
          }

        case None =>
          val base = Seq(
            "gh",
            "issue",
            "list",
            "--state",
            "open",
            "--label",
            label,
            "--json",
            "number,title,labels,updatedAt",
            "--limit",
            limit.toString,
          )
          val cmd = repo.map(r => base ++ Seq("--repo", r)).getOrElse(base)

          runGh(cmd) match
            case Left(err) =>
              System.err.println(err)
              break()
            case Right(output) =>
              val json = ujson.read(output)
              val issues = json.arrOpt.getOrElse(Seq.empty)

              if issues.isEmpty then
                println(s"No open issues labeled '$label'.")
                break()

              println("| Issue # | Title | Tags | Last Updated |")
              println("| --- | --- | --- | --- |")

              issues.foreach { issue =>
                val number = asString(issue("number"))
                val title = escapeMd(asString(issue("title")))
                val tags = issue("labels").arrOpt
                  .getOrElse(Seq.empty)
                  .flatMap(label => label.objOpt.flatMap(_.get("name")))
                  .map(asString)
                  .filter(_.nonEmpty)
                  .sorted
                  .mkString(", ")
                val updatedRaw = asString(issue("updatedAt"))
                val updated = dateOnly(updatedRaw)
                println(s"| $number | $title | ${escapeMd(tags)} | $updated |")
              }

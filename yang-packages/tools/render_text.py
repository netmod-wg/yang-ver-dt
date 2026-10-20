#!/usr/bin/env python3
"""Render an xml2rfc draft as text with the IETF Author Tools API."""

import argparse
import json
import mimetypes
import os
from pathlib import Path
import tempfile
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse
from urllib.request import Request, urlopen
import uuid


API_URL = "https://author-tools.ietf.org/api/render/text"
API_HOST = "author-tools.ietf.org"
DEFAULT_XML = Path(__file__).resolve().parents[1] / "draft-ietf-netmod-yang-packages.xml"


def render(xml_path: Path, output_path: Path) -> None:
    boundary = f"----ietf-author-tools-{uuid.uuid4().hex}"
    mime_type = mimetypes.guess_type(xml_path.name)[0] or "application/xml"
    file_bytes = xml_path.read_bytes()
    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="{xml_path.name}"\r\n'
        f"Content-Type: {mime_type}\r\n\r\n"
    ).encode() + file_bytes + f"\r\n--{boundary}--\r\n".encode()

    request = Request(
        API_URL,
        data=body,
        headers={
            "Accept": "application/json",
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        },
        method="POST",
    )
    try:
        with urlopen(request, timeout=120) as response:
            result = json.load(response)
    except (HTTPError, URLError) as error:
        details = error.read().decode("utf-8", errors="replace") if isinstance(error, HTTPError) else str(error)
        raise RuntimeError(f"Author Tools render request failed: {details}") from error

    logs = result.get("logs", {})
    for warning in logs.get("warnings", []):
        print(f"Author Tools warning: {warning}")
    errors = logs.get("errors", [])
    if errors:
        raise RuntimeError("Author Tools reported errors:\n" + "\n".join(errors))

    output_url = result.get("url")
    parsed_url = urlparse(output_url or "")
    if parsed_url.scheme != "https" or parsed_url.hostname != API_HOST:
        raise RuntimeError(f"Author Tools returned an unexpected output URL: {output_url!r}")

    try:
        with urlopen(output_url, timeout=120) as response:
            rendered = response.read()
    except (HTTPError, URLError) as error:
        details = error.read().decode("utf-8", errors="replace") if isinstance(error, HTTPError) else str(error)
        raise RuntimeError(f"Could not download rendered text: {details}") from error

    rendered.decode("utf-8")  # Fail before replacing the existing output if the response is not text.
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_mode = output_path.stat().st_mode & 0o777 if output_path.exists() else 0o644
    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(dir=output_path.parent, delete=False) as temp_file:
            temp_path = Path(temp_file.name)
            temp_file.write(rendered)
        os.chmod(temp_path, output_mode)
        os.replace(temp_path, output_path)
    finally:
        if temp_path is not None and temp_path.exists():
            temp_path.unlink()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("xml", nargs="?", type=Path, default=DEFAULT_XML, help="input xml2rfc file")
    parser.add_argument("-o", "--output", type=Path, help="output text file (default: XML filename with .txt)")
    args = parser.parse_args()

    xml_path = args.xml.resolve()
    output_path = (args.output or xml_path.with_suffix(".txt")).resolve()
    if not xml_path.is_file():
        parser.error(f"input XML file does not exist: {xml_path}")
    try:
        render(xml_path, output_path)
    except (OSError, RuntimeError, UnicodeError) as error:
        parser.exit(1, f"render_text.py: error: {error}\n")
    print(f"Wrote {output_path}")


if __name__ == "__main__":
    main()

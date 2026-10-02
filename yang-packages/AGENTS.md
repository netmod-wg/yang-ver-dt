# YANG Packages draft instructions

Use `yang-packages/build.mill` to regenerate draft contents. From the repository root, run:

```sh
cd yang-packages
../mill DraftXml.all
```

This target checks the YANG modules, regenerates package examples and tree diagrams, updates the XML draft, and renders the sibling `.txt` file. The standalone YANG files and package definitions in `build.mill` are the sources for generated content embedded in the XML.

For prose-only XML changes, render the text without regenerating embedded content by running `../mill DraftXml.renderText` from `yang-packages`.

Rendering uploads the full draft XML to the IETF Author Tools text-render endpoint at `author-tools.ietf.org` and downloads the rendered text. The user has authorized these uploads to IETF Author Tools / RFC Editor tools for draft generation; no additional confirmation is needed for this workflow. Network access to `author-tools.ietf.org` is required.

Review the XML and generated text together, and include both in the same commit when the XML change is intended to update the published draft. Do not edit the generated `.txt` by hand.

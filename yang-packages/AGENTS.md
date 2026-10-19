# YANG Packages draft instructions

After every change, run Mill using `yang-packages/build.mill` to rebuild the `.txt` draft before presenting the diff or committing. From the repository root, run:

```sh
cd yang-packages
../mill DraftXml.all
```

This target checks the YANG modules, regenerates package examples and tree diagrams, updates the XML draft, and renders the sibling `.txt` file. The standalone YANG files and package definitions in `build.mill` are the sources for generated content embedded in the XML.

For prose-only XML changes, render the text without regenerating embedded content by running `../mill DraftXml.renderText` from `yang-packages`.

Building updated TXT or HTML drafts, including uploading the full draft XML to IETF Author Tools / RFC Editor tools for rendering, is authorized and does not require explicit user permission or additional confirmation. The current text-render workflow uploads XML to `author-tools.ietf.org` and downloads the rendered text; network access to that service is required.

Publishing or submitting a new version of the draft to the IETF Datatracker REQUIRES explicit user permission. Authorization to edit, build, render, or upload to IETF Author Tools does not authorize publication or submission to Datatracker.

Review the XML and generated text together, and include both in the same commit when the XML change is intended to update the published draft. Do not edit the generated `.txt` by hand.

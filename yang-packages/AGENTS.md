# YANG Packages draft instructions

`draft-ietf-netmod-yang-packages.xml` is the source for the rendered text draft. After changing the XML, regenerate the sibling `.txt` file with:

```sh
python3 yang-packages/tools/render_text.py
```

The script uploads the XML to the IETF Author Tools text-render endpoint, downloads the returned temporary text URL, and atomically replaces the `.txt` file. It uses only Python's standard library. Network access to `author-tools.ietf.org` is required.

Review the XML and generated text together, and include both in the same commit when the XML change is intended to update the published draft. Do not edit the generated `.txt` by hand.

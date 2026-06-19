# Draft Technical Review TODOs

These items come from a read-only technical review of
`yang-packages/draft-ietf-netmod-yang-packages.xml`, ignoring the example
package content.

## Completeness And Open Issues

- [ ] Remove or resolve the "Open Questions/Issues" section.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:159`

- [ ] Resolve the package instance data schema naming inconsistency.
      The text names `ietf-inst-data-pkg-schema.ypkg`, while Section 9 defines
      `yang-inst-data-schema-pkg@0.1.0.ypkg`, and IANA uses another form.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:869`

- [ ] Decide whether the draft needs a more explicit namespace policy for
      non-IETF package publishers. Current package-name uniqueness guidance may
      be too weak for global uniqueness.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:679`

## Package Resolution Semantics

- [ ] Define equivalence/error handling for duplicate package, module,
      import-only module, and submodule references. The draft says duplicates
      are "expected to be equivalent", but should say whether non-equivalence is
      an error and which metadata is allowed to differ.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:374`

- [ ] Clarify whether the same module name can appear as both implemented and
      import-only, at either the same version or different versions, after
      package resolution.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:386`

- [ ] Tighten deterministic resolution edge cases: merge order across multiple
      included packages, duplicate suppression for mount `additional-feature`
      and `parent-reference`, and conflict handling for non-equivalent duplicate
      metadata.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:524`

- [ ] Clarify automatic module version resolution when Semver comparison
      ignores modifiers/prerelease/build metadata and two versions compare equal
      but are not actually equivalent.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:606`

## Mounts

- [ ] Fully specify `mount-ypath`, including exact path grammar, key omission
      rules, escaping, namespace/prefix handling, and comparison/equality rules.
      This is a core type and currently contains a TODO.
      Reference: `yang-packages/ietf-yang-package-types.yang:119`

## Conformance And Versioning

- [ ] Make package conformance more formal. Define exact conformance,
      conformance with backwards-compatible differences, and conformance
      failure, especially when servers advertise multiple packages, extra
      modules, extra features, deviations, or changed mounts.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:1172`

- [ ] Extend package versioning rules to cover changes to `complete`,
      `mount/inherit-packages`, `mount/additional-feature`,
      `mount/parent-reference`, and adding/removing mounted packages.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:1051`

- [ ] Clarify how deviation modules are represented and applied. The draft says
      deviations are part of the resolved schema, but the package model has no
      explicit deviation list. State whether including a deviation module implies
      its deviations apply and how this maps to YANG Library.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:1177`

## Retrieval, Caching, And Security

- [ ] Add guidance for package authenticity and integrity. Clients may use
      server-advertised, retrieved, or cached package files, but the draft should
      cover trust, stale caches, conflicting same-name/version files, and secure
      retrieval.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:766`

- [ ] Update the YANG module security section to explicitly identify
      `/packages/package` and the YANG Library package augment as potentially
      sensitive readable data, since the surrounding text already notes that
      package information can help fingerprint devices.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:2723`

## IANA

- [ ] Specify the new IANA YANG package registry more completely: registration
      template, initial contents, designated expert guidance for "Specification
      Required", allowed prefixes, and what qualifies as a stable package-file
      location.
      Reference: `yang-packages/draft-ietf-netmod-yang-packages.xml:2839`

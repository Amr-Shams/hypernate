# Rich Query Builder Challenge Artifacts

This page collects the challenge-related artifacts kept in this solution repo.

The maintainer requested that challenge-related documentation be included in the
repository itself, so this page acts as the index.

## Design Artifacts

- [Rich Query Builder Proposal](rich-query-builder-proposal.md)

Key sections in the proposal:

- [Mentorship Fit](rich-query-builder-proposal.md#mentorship-fit)
- [API Shape](rich-query-builder-proposal.md#api-shape)
- [Architecture](rich-query-builder-proposal.md#architecture)
- [Type Scoping With `docType`](rich-query-builder-proposal.md#type-scoping-with-doctype)
- [Tradeoffs](rich-query-builder-proposal.md#tradeoffs)
- [Remaining Work](rich-query-builder-proposal.md#remaining-work)

## Implemented Slice

Current code delivers a functional architectural vertical slice:

- `Registry.richQuery(Class<T>)` entrypoint
- metadata-backed validation
- `where(...).is(...)` condition support
- automatic `docType` discriminator persistence in stored JSON
- selector generation through `buildSelector()` with `docType` scoping
- typed result materialization through `execute()` via `RichQueryExecutor`

Relevant source files:

- [Registry.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/registry/Registry.java)
- [RichQueryBuilder.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/registry/query/RichQueryBuilder.java)
- [ConditionStep.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/registry/query/ConditionStep.java)
- [RichQueryExecutor.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/registry/query/RichQueryExecutor.java)
- [JSON.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/util/JSON.java)
- [EntityTypes.java](/home/amraly/projects/hypernate/lib/src/main/java/hu/bme/mit/ftsrg/hypernate/util/EntityTypes.java)

## Tests

- [RichQueryBuilderTest.java](/home/amraly/projects/hypernate/lib/src/test/java/hu/bme/mit/ftsrg/hypernate/registry/query/RichQueryBuilderTest.java)
- [RegistryTest.java](/home/amraly/projects/hypernate/lib/src/test/java/hu/bme/mit/ftsrg/hypernate/RegistryTest.java)

The tests verify:

- selector generation for `where(...).is(...)`
- automatic `docType` injection into selectors
- real `execute()` materialization from Fabric `getQueryResult`
- `docType` persistence during entity creation/update
- early rejection of unknown fields

## Scope Statement

This solution prioritizes one end-to-end architectural slice:

- correctness through the `docType` storage contract
- field safety through metadata-backed validation
- materialization through the JSON leniency and execution path

It intentionally defers:

- `rangeQuery()` implementation
- full operator surface (greaterThan, in, etc)
- pagination and full sort/index handling
- AST refactor for complex logical composition
- separate rich/range condition-step APIs for backend-specific operators

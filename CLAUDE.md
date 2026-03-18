# CLAUDE.md — Bonita CMIS Connector

## Project Overview

- **Name:** Bonita CMIS Connector
- **Description:** CMIS Connectors for Bonita — enables interactions in Bonita BPM processes with any CMIS-compatible CMS (e.g., Alfresco, Documentum)
- **Group/Artifact:** `org.bonitasoft.connectors:bonita-connector-cmis`
- **Current version:** 3.0.6-SNAPSHOT
- **License:** GPL v2
- **Java:** 11
- **Tech stack:** Java 11, Apache Chemistry OpenCMIS 1.1.0, Bonita Engine 7.14.0, JUnit 5, Mockito 5, AssertJ, JaCoCo, Maven Assembly Plugin, SonarCloud

## Build Commands

```bash
# Full build (compile + test + package)
./mvnw clean verify

# Skip tests
./mvnw clean verify -DskipTests

# Run only tests
./mvnw test

# Check license headers
./mvnw validate

# SonarCloud analysis (CI only)
./mvnw sonar:sonar
```

The default Maven goal is `verify`. The build produces per-connector zip assemblies in `target/`.

## Architecture

### Class hierarchy

```
org.bonitasoft.engine.connector.AbstractConnector   (Bonita Engine SDK)
  └── AbstractCMISConnector                          (src/main/java/.../cmis/)
        ├── CreateFolder
        ├── DeleteFolder
        ├── DeleteDocument
        ├── DeleteVersionOfDocument
        ├── DownloadDocument
        ├── ListDocuments
        ├── UploadNewDocument
        ├── UploadNewDocuments
        └── UploadNewVersionOfDocument
```

### CMIS client layer

```
AbstractCmisClient   (src/main/java/.../cmis/cmisclient/)
  ├── AtompubCMISClient      — AtomPub binding
  └── WebserviceCMISClient   — Web Services binding
CMISParametersValidator      — validates connector input parameters
```

### Key patterns

- **`AbstractCMISConnector`** handles connection lifecycle (`connect`/`disconnect`), input parameter extraction, and validation. Concrete connectors only implement `executeBusinessLogic()`.
- **`AbstractCmisClient`** wraps an Apache Chemistry `Session`; subclasses configure the session binding (AtomPub vs. WS).
- Connector inputs are passed via `setInputParameters(Map<String, Object>)` before `connect()`.
- Outputs are set via `setOutputParameter(key, value)` inside `executeBusinessLogic()`.
- Resource descriptors (`.def`, `.impl`, `.properties` i18n files) live in `src/main/resources` and `src/main/resources-filtered`; filtered resources use Maven property substitution for IDs/versions.
- Each connector is packaged independently via `src/assembly/*-assembly.xml`.

## Testing Strategy

- Framework: JUnit 5 + Mockito + AssertJ
- Tests live in `src/test/java/org/bonitasoft/connectors/cmis/`
- Unit tests mock `AbstractCmisClient` and Bonita Engine APIs; no live CMIS server required
- Coverage reported by JaCoCo (`target/site/jacoco/`)
- To add a test: extend the connector class under test, mock its `getClient()` return value, call `executeBusinessLogic()`, and assert outputs via `getOutputParameters()`

## Commit Message Format

This project uses **Conventional Commits**:

```
<type>(<scope>): <short description>

Types: feat | fix | chore | docs | refactor | test | ci | build | perf
Scope: optional, e.g. upload, download, ci
```

Examples:
- `feat(upload): support batch document upload`
- `fix(client): handle null session on disconnect`
- `chore(ci): add Claude Code review workflows`

Commit messages are validated by the `commit-message-check.yml` workflow.

## Release Process

1. On the release branch, remove `-SNAPSHOT` from the version in `pom.xml`
2. Trigger the [release workflow](https://github.com/bonitasoft/bonita-connector-cmis/actions/workflows/release.yml) with the target version
3. Merge back to `master` with the next `-SNAPSHOT` version
4. Update the [Bonita marketplace repository](https://github.com/bonitasoft/bonita-marketplace)

The release workflow delegates to the reusable `_reusable_release_connector.yml` from `bonitasoft/github-workflows`.

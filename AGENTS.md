# AGENTS.md - AI Agent Guide for Tools Codebase

## Project Overview

**tools** is a multi-module Maven project providing reusable Java utilities and CLI applications. It consists of:
- **tools-core**: Shared utility library (19+ classes) for file ops, JSON/XML, logging, timing, environment capture
- **tools-archiving**: CLI app for folder archiving using system executables (7-Zip, RAR, WinRAR)
- **tools-databases**: CLI app and shared utility library for databases access and various operations
- **tools-environment**: CLI app and shared utility library for environment capture
- **tools-incubator**: CLI app and shared utility library for experimental features deemed not to be mature enough
- **tools-json_split**: CLI app for splitting large JSON files using streaming parsing
- **tools-utils**: CLI app for various operations on file system
- **tools-web**: CLI app for splitting large JSON files using streaming parsing

All modules target **Java 26** and publish to Maven Central Repository.

## Architecture & Module Dependencies

```
tools (parent POM)
├── tools-core (io.github.dgp-eu.tools.core)
│   └── No dependencies on other modules; contains all core utilities
├── tools-archiving (io.github.dgp-eu.tools.archiving)
│   └── Depends on: tools-core
├── tools-databases (io.github.dgp-eu.tools.databases)
│   └── Depends on: tools-core
├── tools-environment (io.github.dgp-eu.tools.environment)
│   └── Depends on: tools-core
├── tools-incubator (io.github.dgp-eu.tools.incubator)
│   └── Depends on: tools-core
└── tools-json_split (io.github.dgp-eu.tools.json_split)
    └── Depends on: tools-core
└── tools-utils (io.github.dgp-eu.tools.utils)
    └── Depends on: tools-core
└── tools-web (io.github.dgp-eu.tools.web)
    └── Depends on: tools-core
    └── Depends on: tools-databases
    └── Depends on: tools-environment
```

**Critical Pattern**: All utilities exposed through public static classes with inner SubClasses:
- `BasicStructuresClass.SortingSubClass`, `BasicStructuresClass.ConversionSubClass`
- `FileOperationsClass.RetrievingSubClass`, `FileOperationsClass.WritingSubClass`
- `JsonOperationsClass.ArraySubClass`, `JsonOperationsClass.ObjectSubClass`

CLI apps (archiving, json_split) use **picocli** for command parsing; extend AbstractApplication base class.

## Build & Test Workflow

```bash
# From workspace root (C:\www\Data\GitRepositories\GitHub\dgp-eu\Java\tools)

# Build all modules
mvn clean package

# Run tests with coverage
mvn verify

# Security check (CVE scan, CVSS threshold = 7)
mvn dependency-check:check

# Publish to Maven Central (requires gpg profile and env var MAVEN_GPG_PASSPHRASE)
mvn clean package -Pgpg
mvn central-publishing:publish
```

**Test Configuration**:
- Framework: JUnit 6 (Jupiter API)
- Coverage: JaCoCo (configured in root pom.xml)
- Test Classes: Located in `src/test/java/`, named `*Test.java`
- Example: `FileOperationsClassTest` uses `@Test` and `@DisplayName`
- VM Argument for tests: `--enable-native-access=ALL-UNNAMED` (module access)

## Key Files & Packages

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Parent POM; declares 3 modules & versions for Jackson, JUnit, SQLite, Picocli, Log4j, JaCoCo |
| `tools-core/src/main/java/org/dgp-eu/tools/core/*` | Core utility classes (BasicStructure, FileOps, JsonOps, etc.) |
| `tools-core/src/test/java/org/dgp-eu/tools/core/FileOperationsClassTest.java` | Example JUnit 6 tests |
| `tools-archiving/src/main/java/org/dgp-eu/tools/archiving/Application.java` | Entry point; picocli @Command |
| `tools-databases/src/main/java/org/dgp-eu/tools/databases/Application.java` | Entry point; picocli @Command |
| `tools-environment/src/main/java/org/dgp-eu/tools/environment/Application.java` | Entry point; picocli @Command |
| `tools-incubator/src/main/java/org/dgp-eu/tools/incubator/Application.java` | Entry point; picocli @Command |
| `tools-json_split/src/main/java/org/dgp-eu/tools/json_split/Application.java` | Entry point; picocli @Command |
| `tools-utils/src/main/java/org/dgp-eu/tools/utils/Application.java` | Entry point; picocli @Command |
| `tools-web/src/main/java/org/dgp-eu/tools/web/Application.java` | Entry point; picocli @Command |

## Project-Specific Conventions

### Null Safety
- Use `@Nullable` and `@Nonnull` from `org.jspecify` (v1.0.0) for compile-time null checks
- No Optional usage in public APIs; explicit nullable annotations preferred

### CLI Command Structure (Picocli)
```java
@Command(name = "archive-folders", description = "Archive folders...")
class ArchiveFolders implements Runnable {
    @Option(names = {"--folderName"}, required = true, arity = "1..*")
    List<String> folderNames;
    
    @Override
    public void run() { /* Implementation */ }
}
```
Commands are nested subcommands in `Application.java` class using `@Command(subcommands = {...})`.

### Utility Class Pattern
Large utility classes split logic into inner SubClasses for organization:
```java
public class FileOperationsClass {
    public static class RetrievingSubClass { /* read/get methods */ }
    public static class WritingSubClass { /* write/create methods */ }
}
```
This avoids creating separate files while maintaining logical grouping.

### Testing Patterns
- Use `@DisplayName("human-readable description")` for test clarity
- Test edge cases explicitly (null, non-existent paths, permission errors)
- Use `assertEquals`, `assertTrue` from JUnit 5 static assertions
- Temp files: use `Path.of(System.getProperty("java.io.tmpdir"), uniqueName)` to avoid conflicts

### Logging
- Uses Log4j 2 (via log4j-slf4j2-impl binding) with SLF4J API
- Log files stored in `logs/` (dgp_eu__tools-*.log pattern)
- Not configured in this codebase (relies on default or external configuration)

### Dependency Management
- All dependency versions centralized in root `pom.xml` `<dependencyManagement>`
- Child POMs use version-less `<dependency>` declarations
- Critical versions: Jackson 3.2.0, JUnit Jupiter 6.1.0, Java 26

### Module Publishing
- GPG signing enabled via `-Pgpg` profile
- Dependencies: maven-gpg-plugin, takari-lifecycle-plugin (for sources JAR), maven-javadoc-plugin
- Central Publishing: Sonatype Central Publishing Maven Plugin (autoPublish, waitUntil=published)
- Environment variable: `MAVEN_GPG_PASSPHRASE` (set at CI/deployment time)

## Cross-Module Communication

**tools-archiving & tools-json_split** depend on **tools-core** but do NOT depend on each other.
- No circular dependencies
- Each child module creates self-contained JAR with dependencies via maven-assembly-plugin
- Tools available for reuse: `FileOperationsClass`, `JsonOperationsClass`, `BasicStructuresClass`

### Common Utility Access Pattern
```java
// In tools-archiving or tools-json_split
import io.github.dgp-eu.tools.core.*;

// Use static methods from core utilities
String fileSize = FileOperationsClass.RetrievingSubClass.getFileSizeFromPath(path);
List<Path> items = FileOperationsClass.RetrievingSubClass.getSubFolders(folderPath);
```

## Integration Points & External Dependencies

| Dependency | Version | Used For | Scope |
|------------|---------|----------|-------|
| Jackson (databind + dataformat-xml) | 3.2.0 | JSON/XML parsing and generation | compile |
| SQLite JDBC | 3.53.2.0 | Database operations (sqlite-jdbc) | compile |
| Picocli | 4.7.7 | CLI command parsing and help | compile |
| Undertow Core | 2.4.1 | Lightweight web server (Java Web UI) | compile |
| OSHI Core FFM | 7.3.1 | OS info capture (system memory, CPU, etc.) | compile |
| JTE | 3.2.4 | Java Template Engine (web UI rendering) | compile |
| Log4j SLF4J2 | 2.26.0 | Logging via SLF4J + Log4j backend | compile |
| JUnit Jupiter | 6.1.0 | Testing framework | test |
| JaCoCo | 0.8.15 | Code coverage measurement | test |
| JSpecify | 1.0.0 | Null-safety annotations | compile |
| Maven Model | 3.9.16 | POM file parsing (tools-core features) | compile |
| Plexus Interpolation | 1.29 | String interpolation utilities | compile |

**Security Scanning**: OWASP Dependency-Check Maven plugin (v12.2.2) runs in verify phase; fails build if CVE CVSS >= 7.

## Common Workflows for Agent-Assisted Development

### Adding a New Utility to tools-core
1. Create class in `tools-core/src/main/java/org/dgp-eu/tools/core/NewUtilityClass.java`
2. Organize methods into inner static SubClasses (e.g., `RetrievingSubClass`, `ProcessingSubClass`)
3. Add unit tests in `tools-core/src/test/java/org/dgp-eu/tools/core/NewUtilityClassTest.java`
4. Use `@Nullable`/`@Nonnull` annotations
5. Run: `mvn -f tools-core/pom.xml clean verify`

### Adding a Feature to tools-archiving or tools-json_split
1. Add picocli @Command subcommand class
2. Register in Application.java `@Command(subcommands = {NewCommand.class, ...})`
3. Implement logic using core utilities (`FileOperationsClass`, `JsonOperationsClass`, etc.)
4. Create test class in `src/test/java`
5. Run: `mvn verify` (or module-specific: `mvn -f tools-archiving/pom.xml verify`)

### Running Security Scan
```bash
# Set environment variable (optional; uses free tier if not set)
# $env:NVD_API_KEY = "your-nvd-api-key"

mvn dependency-check:check
# Report: target/dependency-check-report.html
```

### Deploying to Maven Central
1. Ensure GPG key configured locally
2. Set `MAVEN_GPG_PASSPHRASE` environment variable
3. Run: `mvn clean package -Pgpg`
4. Artifacts signed and deployed via central-publishing-maven-plugin
5. Auto-published to Maven Central (respects `waitUntil=published`)

## Important Notes for AI Agents

- **Module Isolation**: tools-core has NO dependencies on archiving or json_split; keep it generic
- **Test Execution**: Use `@DisplayName` for debugging; tests require `--add-opens` JVM arg (already configured)
- **Null Handling**: Return error codes (e.g., -99 for null, -3 for missing) rather than throwing exceptions in utilities
- **Version Consistency**: Always update root pom.xml `<dependencyManagement>` for new major dependencies
- **Assembly Output**: JAR artifacts end in `-jar-with-dependencies.jar` to include all transitive deps
- **Logging**: Log4j config not in repo; relies on Log4j2 defaults (console output) or external configuration
- **Java 26 Compatibility**: This project targets bleeding-edge Java; use `var`, records, sealed classes as appropriate

## Testing with IDE/CLI

```bash
# Run tests for specific module
mvn -f tools-core/pom.xml test

# Run single test class
mvn -f tools-core/pom.xml test -Dtest=FileOperationsClassTest

# Run with coverage report
mvn -f tools-core/pom.xml clean verify
# Coverage: target/site/jacoco/index.html
```


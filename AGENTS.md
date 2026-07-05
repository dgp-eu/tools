# AGENTS.md - AI Agent Guide for Tools Codebase

## Project Overview

**tools** is a multi-module Maven project providing reusable Java utilities and CLI applications. It consists of:
- **tools-core**: Shared utility library (12 classes with 23+ SubClasses) for file operations, JSON/XML, logging, timing, environment capture, regex, shelling, HTML, web server utilities
- **tools-archiving**: CLI app for folder archiving using system executables (7-Zip, RAR, WinRAR)
- **tools-cli**: CLI shared utility library for operations
- **tools-databases**: Shared utility library for database access and operations
- **tools-databases-demo**: CLI app for database access and operations
- **tools-environment**: CLI app and shared utility library for system environment capture
- **tools-incubator**: CLI app and shared utility library for experimental features not yet production-ready
- **tools-json_split**: CLI app for splitting large JSON files using streaming parsing
- **tools-undertow**: Shared utility library for web server operations using Undertow and JTE
- **tools-utils**: CLI app for various file system operations
- **tools-web**: CLI app providing web interface for system information and utilities (Undertow + JTE)

All modules target **Java 26** and publish to Maven Central Repository.

## Architecture & Module Dependencies

```
tools (parent POM)
├── tools-core (io.github.dgp-eu.tools.core)
│   └── No dependencies on other modules; contains all core utilities
├── tools-archiving (io.github.dgp-eu.tools.archiving)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
├── tools-cli (io.github.dgp-eu.tools.cli)
│   └── Depends on: tools-core
├── tools-databases (io.github.dgp-eu.tools.databases)
│   └── Depends on: tools-core
├── tools-databases-demo (io.github.dgp-eu.tools.databases-demo)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
│   └── Depends on: tools-json
├── tools-environment (io.github.dgp-eu.tools.environment)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
├── tools-incubator (io.github.dgp-eu.tools.incubator)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
├── tools-json (io.github.dgp-eu.tools.json)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
├── tools-json_split (io.github.dgp-eu.tools.json_split)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
├── tools-undertow (io.github.dgp-eu.tools.undertow)
│   └── Depends on: tools-core
├── tools-utils (io.github.dgp-eu.tools.utils)
│   └── Depends on: tools-core
│   └── Depends on: tools-cli
└── tools-web (io.github.dgp-eu.tools.web)
    └── Depends on: tools-core
    └── Depends on: tools-cli
    └── Depends on: tools-databases
    └── Depends on: tools-environment
```

**Critical Pattern**: All utilities exposed through public static classes with inner SubClasses:
- `BasicStructuresClass`: StringConversionSubClass, StringTransformationSubClass, StringCleaningSubClass, StringEvaluationSubClass, ListAndMapSubClass, PropertiesReaderSubClass
- `FileOperationsClass`: RetrievingSubClass, ContentReadingSubClass, ContentWritingSubClass, WritingSubClass, MovingSubClass, DeletingSubClass, MassChangeSubClass, StatisticsSubClass, RetrievingCompactOrRegularFileSubClass
- `RegularExpressionsClass`: ConversionSubClass, ValidationSubClass
- `TimingClass`: ConversionSubClass, LocalizationSubClass
- `ProjectClass`: ApplicationSubClass, LoaderSubClass, ComponentsSubClass

CLI apps (archiving, json_split, etc.) use **picocli** for command parsing; extend AbstractApplication base class.

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

| File                                                                                  | Purpose                                                                                                                     |
|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `pom.xml` (root)                                                                      | Parent POM; declares 11 modules & versions for Jackson, JUnit, SQLite, Picocli, Log4j, JaCoCo, and other build dependencies |
| `tools-core/src/main/java/io/github/dgp-eu/tools/core/*`                              | Core utility classes: BasicStructures, FileOperations, JsonOperations, Timing, Shelling, ProjectClass, UndertowClass, etc.  |
| `tools-core/src/main/resources/project.properties`                                    | Windows-specific configuration (System32 paths, PowerShell location)                                                        |
| `tools-core/src/test/java/org/dgp-eu/tools/core/FileOperationsClassTest.java`         | Example JUnit 6 tests                                                                                                       |
| `tools-archiving/src/main/java/org/dgp-eu/tools/archiving/Application.java`           | Entry point; picocli @Command                                                                                               |
| `tools-cli/src/main/java/org/dgp-eu/tools/cli/CommonApplication.java`                 | Entry point; picocli @Command                                                                                               |
| `tools-databases/src/main/java/org/dgp-eu/tools/databases/*`                          | Database functionality                                                                                                      |
| `tools-databases-demo/src/main/java/org/dgp-eu/tools/databases/demo/Application.java` | Entry point; picocli @Command                                                                                               |
| `tools-environment/src/main/java/org/dgp-eu/tools/environment/Application.java`       | Entry point; picocli @Command                                                                                               |
| `tools-incubator/src/main/java/org/dgp-eu/tools/incubator/Application.java`           | Entry point; picocli @Command                                                                                               |
| `tools-json/src/main/java/org/dgp-eu/tools/json/Application.java`                     | Entry point; picocli @Command                                                                                               |
| `tools-json_split/src/main/java/org/dgp-eu/tools/json_split/Application.java`         | Entry point; picocli @Command                                                                                               |
| `tools-undertow/src/main/java/io/github/dgp-eu/tools/undertow/*`                      | Undertow and JTE wrapper classes for web server operations (utility library, no CLI)                                        |
| `tools-undertow/src/main/resources/undertow.properties`                               | Web server defaults                                                                                                         |
| `tools-utils/src/main/java/org/dgp-eu/tools/utils/Application.java`                   | Entry point; picocli @Command                                                                                               |
| `tools-web/src/main/java/org/dgp-eu/tools/web/Application.java`                       | Entry point; picocli @Command; Undertow web server integration                                                              |

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

### Project Resources & Configuration
- **tools-core/src/main/resources/project.properties**: Contains Windows-specific paths and web server defaults
  - System32 path: `C:\Windows\System32`
  - PowerShell executable: `C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe`
  - Web server binding IP: `0.0.0.0`
  - Web server protocol: `http`
- Agents should be aware of these defaults when extending tools-web or tools-core functionality

### Dependency Management
- All dependency versions centralized in root `pom.xml` `<dependencyManagement>`
- Child POMs use version-less `<dependency>` declarations
- Critical versions: Jackson 3.2.0 (custom build), JUnit Jupiter 6.1.0, Java 26

### Build Plugin Configuration
- **takari-lifecycle-plugin**: Generates sources JAR automatically during package phase
- **flatten-maven-plugin**: Resolves CI-friendly versions (uses resolveCiFriendliesOnly mode)
- **maven-assembly-plugin**: Creates fat JAR (`*-jar-with-dependencies.jar`) for CLI applications
- **maven-javadoc-plugin**: Generates Javadoc with doclint disabled
- **maven-surefire-plugin**: Configured with JUnit Jupiter 6.1.0 and `--enable-native-access=ALL-UNNAMED` for FFM module access
- **jacoco-maven-plugin**: Code coverage with bundle-level rules check
- **central-publishing-maven-plugin**: Configured for auto-publish to Maven Central with waitUntil=published
- **versions-maven-plugin**: Used for dependency updates and version management

### Module Publishing
- GPG signing enabled via `-Pgpg` profile
- Dependencies: maven-gpg-plugin, takari-lifecycle-plugin (for sources JAR), maven-javadoc-plugin
- Central Publishing: Sonatype Central Publishing Maven Plugin (autoPublish, waitUntil=published)
- Environment variable: `MAVEN_GPG_PASSPHRASE` (set at CI/deployment time)

## Cross-Module Communication

**tools-archiving & tools-json_split** depend on **tools-core** but do NOT depend on each other.
- No circular dependencies
- Each child module creates self-contained JAR with dependencies via maven-assembly-plugin
- Tools available for reuse: `FileOperationsClass`, `BasicStructuresClass`

### Common Utility Access Pattern
```java
// In tools-archiving or tools-json_split
import io.github.dgp_eu.tools.core.*;

// Use static methods from core utilities
String fileSize = FileOperationsClass.RetrievingSubClass.getFileSizeFromPath(path);
List<Path> items = FileOperationsClass.RetrievingSubClass.getSubFolders(folderPath);
```

## Integration Points & External Dependencies

| Dependency                                                           | Version  | Used For                                             | Scope   |
|----------------------------------------------------------------------|----------|------------------------------------------------------|---------|
| Jackson Core (tools.jackson.core:jackson-databind)                   | 3.2.0    | JSON parsing and generation (custom build)           | compile |
| Jackson DataFormat (tools.jackson.dataformat:jackson-dataformat-xml) | 3.2.0    | XML serialization/deserialization (custom build)     | compile |
| SQLite JDBC                                                          | 3.53.2.0 | Database operations (sqlite-jdbc) in tools-databases | compile |
| Picocli                                                              | 4.7.7    | CLI command parsing and help                         | compile |
| Undertow Core                                                        | 2.4.2    | Lightweight web server (tools-web Java Web UI)       | compile |
| OSHI Core FFM                                                        | 7.3.2    | OS info capture (system memory, CPU, etc.)           | compile |
| JTE                                                                  | 3.2.4    | Java Template Engine (tools-web UI rendering)        | compile |
| Log4j Core                                                           | 2.26.1   | Logging implementation via SLF4J adapter             | compile |
| Log4j SLF4J2 Adapter                                                 | 2.26.1   | SLF4J 2.0 API binding to Log4j 2 Core                | compile |
| Maven Model                                                          | 3.9.16   | POM file parsing (tools-core features)               | compile |
| Plexus Interpolation                                                 | 1.29     | String interpolation utilities                       | compile |
| JUnit Jupiter                                                        | 6.1.1    | Testing framework                                    | test    |
| JaCoCo                                                               | 0.8.15   | Code coverage measurement                            | test    |
| JSpecify                                                             | 1.0.0    | Null-safety annotations                              | compile |

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
3. Implement logic using core utilities (`FileOperationsClass`, `BasicStructuresClass`, etc.)
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


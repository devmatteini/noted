# Gradle

Gradle is the build orchestrator around Kotlin/Java/Android (https://github.com/gradle/gradle).

In this Android project it handles:

- downloading dependencies
- compiling Kotlin
- compiling Android resources
- running tests
- packaging APKs
- signing/build variants like debug/release
- running Android Gradle Plugin tasks

Analogy:

- Node: npm/pnpm scripts + dependency install + build pipeline
- Rust: cargo, but more configurable and more verbose
- Make: a modern dependency-aware build system

Key files:

- settings.gradle.kts: workspace/modules
- build.gradle.kts: root build config
- app/build.gradle.kts: app build config, closest to package.json/Cargo.toml
- gradle/libs.versions.toml: dependency/plugin versions
- gradlew: wrapper command you run

Use the wrapper:

```shell
./gradlew :app:assembleDebug
```

Meaning:

- `:app` = app module
- `assembleDebug` = build debug APK

## Dependency Catalog

`gradle/libs.versions.toml` defines dependency names and versions.

Example:

```toml
[libraries]
junit = { group = "junit", name = "junit", version = "4.13.2" }
```

This alone does not add the dependency to the app. It only creates a catalog alias.

`app/build.gradle.kts` activates the dependency for the app module:

```kotlin
dependencies {
    testImplementation(libs.junit)
}
```

Analogy:

- `libs.versions.toml` = centralized dependency/version catalog
- `app/build.gradle.kts dependencies { ... }` = actually adding the package

## Dependency Scopes

Common scopes:

- `implementation`: production dependency, usable by app code and tests.
- `testImplementation`: local JVM test dependency, usable only under `app/src/test`.
- `androidTestImplementation`: Android device/emulator test dependency, usable only under
  `app/src/androidTest`.
- `debugImplementation`: debug-build-only dependency, not included in release builds.
- `releaseImplementation`: release-build-only dependency.
- `compileOnly`: needed to compile, not packaged at runtime.
- `runtimeOnly`: packaged/available at runtime, not needed to compile.

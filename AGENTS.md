# AGENTS.md

Noted is a single-module Android app for quick notes, built with Kotlin, Jetpack Compose, Room, and
Gradle.

## Essentials

- Package/application id: `com.cosimomatteini.noted`.
- Package manager/build runner: Gradle wrapper (`./gradlew`), with Make shortcuts in `Makefile`.
- Only included module is `:app`; app entrypoint is
  `app/src/main/java/com/cosimomatteini/noted/MainActivity.kt`.
- Manual DI is wired in `app/src/main/java/com/cosimomatteini/noted/NotedAppContainer.kt`; do not
  add Hilt unless explicitly requested.
- Keep [ARCHITECTURE.md](./ARCHITECTURE.md) in sync, but verify roadmap claims against code before
  treating them as implemented.

## More Context

- Architecture rules: [docs/agents/architecture.md](docs/agents/architecture.md)
- Persistence: [docs/agents/persistence-ui.md](docs/agents/persistence.md)

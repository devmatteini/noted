# Contributing

Welcome, we really appreciate if you're considering to contribute to `Noted`!

## Development

1. Fork this repo
2. Clone your forked repo

```shell
git clone https://github.com/<username>/noted && cd noted
```

3. Create a new branch with `git checkout -b my-branch`
4. Configure git hooks so pre-commit runs formatting

```shell
make configure-hooks
```

5. Build project with `make build`

Other `make` targets:

- `check`: run ktlint, unit tests, and debug build
- `test`: run unit tests
- `android-test`: run connected Android tests
- `lint`: run ktlint
- `format`: format code with ktlint

Useful commands when testing with Android Emulator:

- `clear-app-data`: clear installed app data
- `generate-fixture`: generate a backup fixture file that you con import with different use cases
- `upload-fixture`: upload fixture file to device download directory
- `copy-export`: copy today's exported backup from device download directory

## Code Style

- Always run `make format` before committing (see above to configure git hooks)
- Keep changes small and focused
- Prefer simple Kotlin and Compose code
- Split changes into atomic commits where build and tests pass
- Use concise commit messages with format `area: summary`

## Architecture & Technical Choices

Read [ARCHITECTURE.md](./ARCHITECTURE.md) to have an overview of the product and the technical
choices.

## License

By contributing, you agree that your contributions will be licensed under [MIT License](LICENSE).

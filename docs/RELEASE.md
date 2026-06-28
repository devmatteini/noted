# Release

Steps to create a new release of `Noted`:

1. Pull remote changes with `git pull --rebase`
2. Compile and run tests with `make check`
3. Update [CHANGELOG.md](../CHANGELOG.md) with new release changes and run
   `git add CHANGELOG.md && git commit -m "changelog: update for release <version>"`
4. Make sure [README.md](../README.md) is updated
5. Bump version in `app/build.gradle.kts`
    - `versionCode`: increment by 1
    - `versionName`: new version following Semver
6. Run `git add app/build.gradle.kts && git commit -m "release <version>"`
7. Push to remote with `git push`
8. Create signed tag `git tag -s <version> -m "See CHANGELOG.md for more details"`
9. Wait CI to pass all checks and then run `git push --tags`
10. Wait release workflow to complete and update release page by copying
    from [CHANGELOG.md](../CHANGELOG.md) the release notes

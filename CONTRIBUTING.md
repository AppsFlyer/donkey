# Contributing to Donkey

----

Thank you for wanting to contribute to Donkey. We welcome you to contribute bug
fixes, code enhancements, new features, tests, documentation, usage examples,
and so on. All contributions are done via Pull Requests (PR).

Before opening a PR please open
an [issue](https://github.com/AppsFlyer/donkey/issues)
that addresses the changes that you are proposing. Once it is agreed that the
change is needed proceed to submitting a PR.

## Submitting a PR

All PRs will be reviewed by project admins. Follow the steps below to submit
your PR.

- Create a **fork** of the repository.
- Checkout the `master` branch and pull the latest changes.
- We follow the [semantic versioning](https://semver.org/) convention. Use it to
  decide what the next version should be. For example, assuming the current
  version is `0.3.0`, and you would like to create a PR for a bug, then the next
  version would be `0.3.1`.  
  On a *_nix_ system, run the `dev-branch.sh` script to create a new development
  branch. For example:
  ```shell
    ./dev-branch 0.3.1
  ```
  The script will create a branch `0.3.1` and update the project to version
  `0.3.1-SNAPSHOT`. For those who work on a Windows system, you'll have to do
  this manually :).

  **We will be happy to accept a PR that adopts the script to Windows system as
  well.
- See the README.md for how to [build](README.md#building) the project and
  minimum [requirements](README.md#requirements).
- Commit your changes with a short and meaningful commit message.
- All but the most trivial code changes should be accompanied by tests.
- Run `mvn clean test` and confirm all tests pass.
- Open a new PR from your fork to the base project's `master` branch.
- Write a clear description of the nature of the changes, and submit it.

An admin project member will review the PR and either request changes or approve
and merge it.

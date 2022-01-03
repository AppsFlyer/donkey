## v0.5.2

- Depedency updates -
  
  | Dependency | Old Version | New Version |
  | ------- | -----: | -----: |
  | vertx | 4.0.0 | 4.2.3 |
  | clojure | 1.10.1 | 1.10.3 |
  | spec.alpha | 0.2.187 | 0.3.218 |
  | clojure.logging | 1.1.0 | 1.2.3 |
  | junit | 5.7.0 | 5.8.2 |
  | jsonista | 0.3.0 | 0.3.5 |
  | slf4j | 1.7.30 | 1.7.32 |
  | logback | 1.2.3 | 1.2.8 |
  | mockito | 3.6.28 | 3.12.4 |
  | ring-json | 0.5.0 | 0.5.1 |
  | ring-core | 1.8.2 | 1.9.4 |

## v0.5.1

- Enhancement. Added `follow-redirects` option when creating a client.
- Removed logback.xml and other unnecessary files from the final jar.

## v0.5.0

- Enhancement. Added support for returning `java.io.File` and Ring response
  body. Issue #17.
- Enhancement. Added support for defining static resources via a `:resources`
  map when creating a server. The map used when calling `donkey/create-server`
  can include either `:routes` , `:resources` or both. At least one of them is
  required. Issue #17.
- Enhancement. Added a `destroy` function to `IDonkey` protocol, which releases
  all the resources associated with instance. Issue #19.

## v0.4.2

- Bug fix. `remote-addr` field in Ring request map includes client port.

## v0.4.1

- Moved from Travis CI to GitHub Actions

## v0.4.0

- Debug mode is now set when creating a `Donkey` instance rather than creating a `DonkeyServer` or `DonkeyClient`. This fixes issues where enabling debug
  mode on the server / client and not on the other would sometimes disable debug
  mode for both.
- Turned on Clojure spec assertions
- Global exception handler - In debug mode, a stack trace is added to the
  exception if it doesn't include one.
- Fixed release script not pushing changes to GitHub.

## v0.3.0

- Added support for user-defined error handlers when creating a server. The
  server options support a new field - `:error-handlers`. Users can supply a map
  of http-status-code -> handler-function.

## v0.2.0

- `version-bump.sh` script was removed in favor of:
    - `version-change.sh` Update the project to a new version.
    - `dev-branch.sh` Create a new development branch according to an input
      version.
    - `release.sh` Used to remove `-SNAPSHOT` suffix from project version, add a
      release tag, and deploy to clojars.org.
- Updated the `README.md` documentation.
- Added GitHub issue templates.
- Added TCP configuration options to `com.appsflyer.donkey.core/create-server`:
    - `tcp-no-delay`
    - `tcp-quick-ack`
    - `tcp-fast-open`
    - `socket-linger-seconds`
    - `accept-backlog`
- Change default client connection idle timeout from 60 to 30 seconds.
- Dependency updates

| Dependency | Old Version | New Version |
| ------- | -----: | -----: |
| vertx | 3.9.4 | 4.0.0 |
| junit | 5.6.2 | 5.7.0 |
| jsonista | 0.2.7 | 0.3.0 |
| mockito | 3.4.6 | 3.6.28 |
| jetbrains/annotations | 13.0 | 20.1.0 |
| ring-core | 1.8.1 | 1.8.2 |

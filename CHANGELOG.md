## v0.1.1

---

- Refactored version-bump.sh script into release and new development branch
  scripts
- Updated the README.md documentation
- Added GitHub issue templates
- Added TCP configuration options to `com.appsflyer.donkey.core/create-server`:
    - `tcp-no-delay`
    - `tcp-quick-ack`
    - `tcp-fast-open`
    - `socket-linger-seconds`
    - `accept-backlog`
- Change default client connection idle timeout from 60 to 30 seconds

---
name: horizon-pr
description: Run the Horizon (OpenNMS core) container image that CircleCI built for a pull request or branch, together with PostgreSQL, on the local machine. Use this whenever someone wants to try, test, demo, reproduce a bug on, or look at the UI of a PR or branch without building it locally. Trigger on phrases like "run PR 8964", "start horizon for this branch", "pull the CI image", "spin up the container from CircleCI", "test this PR locally", or any request to start OpenNMS from a CI build. Also use it to stop or clean up such a stack.
---

# Run a PR's Horizon image locally

CircleCI builds a Horizon container image for every pipeline and stores it as the
artifact `horizon-linux-amd64.oci` on the job `horizon-image-single-arch-linux-amd64`.
This skill downloads that artifact, loads it into Docker, and starts it with PostgreSQL
through `docker compose`. Nothing is built locally.

The bundled script does all the steps. Run it; do not reimplement the API calls.

```bash
.claude/skills/horizon-pr/scripts/run-horizon-pr.sh <pr-number|branch> [options]
```

## Prerequisites

Check these before the first run. Report what is missing and how to fix it.

- `docker` with the `compose` plugin, daemon running.
- `gh` logged in (`gh auth status`). Needed only when the argument is a PR number.
- `jq` and `curl`.
- A CircleCI personal API token in `CIRCLE_TOKEN` (or `CIRCLECI_TOKEN`), or in the
  file `~/.circleci/token` (one line, `chmod 600`). Artifact downloads return 401 without it. The CircleCI API itself is public
  for this repo, so resolving the job works without the token.
  Create one at https://app.circleci.com/settings/user/tokens.
  If the user has the CircleCI MCP server configured, the same token is in its
  `CIRCLECI_TOKEN` env entry; ask the user to export it, do not read it from config files.

## Workflow

1. Resolve the target. A number is a PR in OpenNMS/opennms; anything else is a branch name.
2. Run the script. It prints each step: pipeline, job number, artifact URL, download,
   image tag, compose project, then polls the web UI until it answers.
3. Tell the user the URL, credentials `admin` / `admin`, the compose project name, and the
   log command the script prints.

Options:

| Option | Effect |
|---|---|
| `--resolve-only` | Print pipeline, job and artifact URL, then exit. Use to check that CI has an image. |
| `--no-start` | Download and load the image, do not start compose. |
| `--down` | Stop and remove the stack and its volumes for that PR/branch. |
| `--pipeline <number>` | Use a specific pipeline instead of the newest one with a successful image job. |
| `--web-port <port>` | Host port for the web UI. Default 18980. |
| `--ssh-port <port>` | Host port for the Karaf shell. Default 18101. |

## Things to know

- Images for PR branches are amd64 only. On Apple Silicon Docker runs them through
  emulation. Startup takes 5 to 10 minutes and the UI is slow. That is expected.
  arm64 images exist only on `master-*` branches.
- The script picks the newest pipeline whose image job succeeded, even when later jobs
  in that workflow failed. If no pipeline on the branch has a finished image job, the
  script says so. Point the user at the CircleCI workflow; the image job runs after
  `build`, usually 40 to 60 minutes after the push.
- Downloads are cached in `~/.cache/opennms-horizon-pr/<job-number>/`. The OCI file is
  about 1 GB. A second run for the same job skips the download and the load.
- The stack uses shifted host ports by default so it never collides with a local OpenNMS:
  web 18980, Karaf shell 18101, traps 11162/udp, syslog 20514/udp. The container keeps
  the standard ports inside. Change them with `--web-port`, `--ssh-port`,
  `HORIZON_TRAP_PORT` and `HORIZON_SYSLOG_PORT`. The script still checks the ports before
  it starts, which matters when a second stack is already up. The database is not
  published on the host, so a local PostgreSQL on 5432 does not conflict.
- Each PR gets its own compose project, `horizon-pr-<ref>`, and its own volumes, so two
  PRs can be loaded at once but only one can use port 18980. Use `--web-port` for a second.
- The database runs as `postgres` superuser. The Horizon entrypoint creates the
  `opennms` user and database itself on the first start (`-s` flag runs the installer).
- `--down` deletes the volumes. Say this before running it if the user has data they care about.

## Compose stack

`assets/docker-compose.yml` has two services: `database` (postgres:15 with a health check)
and `horizon`. The environment variables mirror what the smoke tests give
`OpenNMSContainer`, so a PR that passes smoke tests starts here with the same settings.
Edit the compose file if a PR needs extra environment, for example
`OPENNMS_TIMESERIES_STRATEGY=newts` or more heap in `JAVA_OPTS`.

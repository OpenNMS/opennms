# CircleCI Configuration

This build is using a number of advanced features of CircleCI to make the CI
pipeline a bit more manageable:

1. [Dynamic Configuration](https://circleci.com/docs/2.0/dynamic-config/)
2. The `circleci config pack` feature of
   [the CircleCI CLI](https://circleci.com/docs/2.0/local-cli/)

## Dynamic Configuration

Dynamic configuration allows us to do some logic at the beginning of a build
to determine what parts of the pipeline to trigger.

There is a small `circleci.yml` file that uses CircleCI's "continuation"
support along with a python scripts to check which parts of the codebase
have been modified, and then sets parameters to be used in a sub-workflow.
(See the `trigger-path-filtering` portions of `config.yml` for an idea of
what it's doing.)

The "main" config file is generated dynamically based on the parameters set and 
user provided properties. For backward compatibility we hae kept the workflows logics
(such as filters) similar to before.

Each of the workflows in the "main" config then uses a `when:` field
referencing various `trigger-*` parameters to enable or disable them.

## Build paths

The user has ability to modify the build path by
1. using git commit:
* You can use the following keywords to enable build path(s)

| Keyword       | Description |
| ------------- | ------------- |
| #smoke        | Enable smoke tests |
| #smoke-flaky  | Enable flaky smoke tests|
| #integration  | Enable integration tests|
| #rpm          | Enable rpm jobs |
| #deb          | Enable debian package jobs |
| #oci          | Enable oci jobs |
| #doc          | Enable doc job  |
| #ui           | Enable ui job |

* The script attempts to detect and enable corresponding jobs if incoming changes contains:
** Changes to "IT.java" or "Test.java" files
** Changes to "doc" or "ui" 
** Changes to "opennms-container"

2. using `build-triggers.override.json` file
* You can enable the jobs you want to run by setting them to True
**Note:** When you enable a job, its dependencies will be enabled.

# Smoke Tests

## Running locally

1. download .oci artifacts
2. load them:
```
docker image load -i minion.oci
docker image load -i sentinel.oci
```

# Future Work

## Speedups

### Cache node artifacts

`node_modules` in `core/web-assets` does not need to be cached because we
use `npm --prefer-offline --no-progress ci` which delete `node_modules` so
 we cache the `~/.npm` directory.
### Weekly / cron triggered jobs

Due to current setup trigger by cron is unavailable and has been replace by
advanced setup in circleci GUI. Currently we have 2 independent weekly jobs:
   - coverage-api for code quality coverage
   - automation for devops operations like docker stale image pruning
These jobs are enabled by setting  trigger-coverage-api / trigger-automation
parameters as true

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
support along with a python script to check which parts of the codebase
have been modified, and then sets parameters to be used in a sub-workflow.
(See the `trigger-path-filtering` portions of `config.yml` for an idea of
what it's doing.)

Each of the workflows in the "main" config then uses a `when:` field
referencing various `trigger-*` parameters to enable or disable them.

## Config Packing

This feature was originally designed for writing CircleCI orbs, self-contained
sets of macros and commands for reuse in CircleCI configs.  It allows you to
make a directory full of YAML files and it packs them up into a single
monolithic YAML file.

For details on the mechanics of config packing, see
[the CircleCI docs](https://circleci.com/docs/2.0/local-cli/#packing-a-config).

The initial implementation just chops the `parameters` and `executors`
sections out of the main config to make it easier to manage merge
conflicts. In the future, ideally, we'd refactor much more of the config
to be easier to edit in pieces.

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

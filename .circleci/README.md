# CircleCI Configuration

This build is using a number of advanced features of CircleCI to make the CI
pipeline a bit more manageable, most notably dynamic config[^1].

## Dynamic Configuration

Dynamic configuration allows us to do some logic at the beginning of a build
to determine what parts of the pipeline to trigger.

There is a small `circleci.yml` file that uses CircleCI's "continuation"
support along with a python scripts to check which parts of the codebase
have been modified, and then sets parameters to be used in a sub-workflow.

The real workflow config file is generated dynamically based on the parameters set and 
user provided properties. For backward compatibility we have kept the workflows logics
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

**Note:** These keywords are hardcoded in `process_generate.py` file located under `pyscripts` folder.

* The script attempts to detect and enable corresponding jobs if incoming changes contains:
** Changes to "IT.java" or "Test.java" files
** Changes to "docs" or "ui" 
** Changes to "opennms-container"

2. using `build-triggers.override.json` file
* You can enable the jobs you want to run by setting them to True
**Note:** When you enable a job, its dependencies will be enabled.

## Understanding the workflows and expanding them
Workflows information is stored in `workflows_v2.json` file located under `main/workflows` folder.
This file is broken down into `bundles` and `individual` sections.
* `bundle` section allows for creating workflow(s) that contain of a set of `individual` job.
* `individual` section allows for defining a job and it's dependencies and filters

The names of `bundle` or `individual` are used in the `generate_main.py` file located under `pyscripts` folder.
**Note:** There is a close relationship between properties defined in `build-triggers.override.json` and the logic used in `generate_main.py`.


# Smoke Tests
See Readme file under smoke-test folder[^2].

# Footnotes

[^1]: [Dynamic Configuration](https://circleci.com/docs/2.0/dynamic-config/)
[^2]: [smoke-test Readme file](../smoke-test/README.md)
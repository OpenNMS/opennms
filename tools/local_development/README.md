# Helper scripts for setting up and bringing up OpenNMS Development Environment

The scripts below build and run OpenNMS on your machine, backed by a Dockerized
PostgreSQL. If you would rather run everything in containers — including
VictoriaMetrics as the time series store and Grafana — see
[`stack/`](stack/README.md).

# `dependencies.sh`

```
$ ./tools/local_development/dependencies.sh --help
Usage: ./tools/local_development/dependencies.sh [options]
Options:
  --help                       Show this help message
  --check-dependencies         Check if required dependencies are installed (default action)
  --deploy-postgresql          Deploy and setup PostgreSQL using Docker
  --install-jrrd2              Install jrrd2 library, from prebuilt binaries
  --install-jrrd2-from-source  Compile and install jrrd2 from source code
  --enable-jrrd2               Update OpenNMS configuration to use jrrd2 library that is detected/installed
```

# `opennms.sh`

**Note:** Script will check if `POSTGRES_PASSWORD` environment variable has been set with the required password for opennms user to connect to Postgres. If it's not set, it will use a default password

```
$ ./tools/local_development/opennms.sh --help
Usage: ./tools/local_development/opennms.sh [options]
Options:
  --help                  Show this help message
  --disable-jrrd2         Disable building jrrd2 library
  --skip-cleanup          Skip cleanup of previous build artifacts
  --enable-tests          Enable running tests during build
```
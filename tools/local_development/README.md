# Helper scripts for setting up and bringing up OpenNMS Development Environment

# `dependencies.sh`

```
$ ./tools/local_development/dependencies.sh --help
Usage: ./tools/local_development/dependencies.sh [options]
Options:
  --help                       Show this help message
  --check-dependencies         Check if required dependencies are installed (default action)
  --deploy-postgresql          Deploy and setup PostgreSQL using Docker
  --enable-rrdtool             Update OpenNMS configuration to use RRDtool (system librrd)
```

# `opennms.sh`

**Note:** Script will check if `POSTGRES_PASSWORD` environment variable has been set with the required password for opennms user to connect to Postgres. If it's not set, it will use a default password

```
$ ./tools/local_development/opennms.sh --help
Usage: ./tools/local_development/opennms.sh [options]
Options:
  --help                  Show this help message
  --disable-rrdtool       Do not configure OpenNMS to use RRDtool
  --skip-cleanup          Skip cleanup of previous build artifacts
  --enable-tests          Enable running tests during build
```
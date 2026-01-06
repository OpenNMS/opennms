# Helper scripts for setting up and bringing up OpenNMS Development Environment


## 

# `dependencies.sh`

```
$ ./tools/local_development/dependencies.sh --help
Usage: ./tools/local_development/dependencies.sh [options]
Options:
  --help                  Show this help message
  --check-dependencies    Check if required dependencies are installed (default action)
  --deploy-postgresql    Deploy and setup PostgreSQL using Docker
  --install-jrrd2         Install jrrd2 library,from prebuilt binaries
  --install-jrrd2-from-source  Compile and install jrrd2 from source code
  --enable-jrrd2        Update OpenNMS configuration to use jrrd2 library that is detected/installed
```

# `opennms.sh`

```
$ ./tools/local_development/opennms.sh --help
Detected OS: macOS
Usage: ./tools/local_development/opennms.sh [options]
Options:
  --help                   Show this help message
  --enable-jrrd2          Enable jrrd2 library,from prebuilt binaries
```
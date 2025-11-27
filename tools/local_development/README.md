# Helper scripts for setting up and bringing up OpenNMS Development Environment


## 

# `dependencies.sh`

```
$ ./tools/local_development/dependencies.sh --help
Usage: ./tools/local_development/dependencies.sh [options]
Options:
  --help                   Show this help message
  --check-dependencies    Check if required dependencies are installed (default action)
  --install-postgresql     Install and setup PostgreSQL using Docker
  --install-jrrd2         Install jrrd2 library,from prebuilt binaries
  --install-jrrd2-from-source  Compile and install jrrd2 from source code
```

# `opennms.sh`

```
❯ ./tools/local_development/opennms.sh --help
Detected OS: macOS
Found jrrd2.jar at /Users/mershad-manesh/Documents/work/tmp/opennms/built_dependencies/jrrd2.jar
Found libjrrd2 at /Users/mershad-manesh/Documents/work/tmp/opennms/built_dependencies/lib/libjrrd2.....
```
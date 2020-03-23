
## Usage

Start by running the `structure-maven-plugin` to generate the Maven project structure and dependency tree out to a `.json` file:
```
mvn org.opennms.maven.plugins:structure-maven-plugin:1.0:structure
```

Output will be rendered to:
```
$ ls -alh target/structure-graph.json
-rw-rw-r--. 1 jesse jesse 618K Aug 14 21:18 target/structure-graph.json
```

Now run:
```
$ python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists .
```

And you should see output like:
```
$ python3 .circleci/scripts/find-tests/find-tests.py generate-test-lists .
Maven project contains 676 modules.
Comparing to: release-25.0.0
Files with changes:
./.nightly
./.circleci/scripts/find-tests/maven.py
./.circleci/scripts/find-tests/README.md
./features/telemetry/daemon/src/main/java/org/opennms/netmgt/telemetry/daemon/Telemetryd.java
./.circleci/scripts/itest.sh
./.circleci/scripts/find-tests/maven_test.py
./.circleci/scripts/find-tests/__init__.py
./.circleci/config.yml
./.circleci/scripts/find-tests.py
./.circleci/scripts/find-tests/find-tests.py
./.circleci/scripts/find-tests/git.py
./.gitignore
./.circleci/scripts/find-tests/sample.json
No module match for ./.nightly
No module match for ./.circleci/scripts/find-tests/maven.py
No module match for ./.circleci/scripts/find-tests/README.md
No module match for ./.circleci/scripts/itest.sh
No module match for ./.circleci/scripts/find-tests/maven_test.py
No module match for ./.circleci/scripts/find-tests/__init__.py
No module match for ./.circleci/config.yml
No module match for ./.circleci/scripts/find-tests.py
No module match for ./.circleci/scripts/find-tests/find-tests.py
No module match for ./.circleci/scripts/find-tests/git.py
No module match for ./.gitignore
No module match for ./.circleci/scripts/find-tests/sample.json
Modules with changes:
org.opennms.features.telemetry:org.opennms.features.telemetry.daemon:25.0.0-SNAPSHOT
Modules to consider:
org.opennms.features.minion:core-repository:25.0.0-SNAPSHOT
org.opennms.features.container:minion:25.0.0-SNAPSHOT
org.opennms.karaf:opennms:25.0.0-SNAPSHOT
org.opennms.features.container:sentinel:25.0.0-SNAPSHOT
org.opennms.features.telemetry:org.opennms.features.telemetry.daemon:25.0.0-SNAPSHOT
org.opennms.features.telemetry.distributed:org.opennms.features.telemetry.distributed.sentinel:25.0.0-SNAPSHOT
org.opennms.container:org.opennms.container.shared:25.0.0-SNAPSHOT
org.opennms.features.minion:repository:25.0.0-SNAPSHOT
org.opennms.features.sentinel:repository:25.0.0-SNAPSHOT
org.opennms.features.telemetry:org.opennms.features.telemetry.itests:25.0.0-SNAPSHOT
org.opennms.container:org.opennms.container.karaf:25.0.0-SNAPSHOT
Modules with tests:
org.opennms.features.telemetry:org.opennms.features.telemetry.itests:25.0.0-SNAPSHOT
        /home/jesse/git/opennms/features/telemetry/itests/src/test/java/org/opennms/netmgt/telemetry/itests/ThresholdingIT.java - org.opennms.netmgt.telemetry.itests.ThresholdingIT (Failsafe)
        /home/jesse/git/opennms/features/telemetry/itests/src/test/java/org/opennms/netmgt/telemetry/itests/ListenerParserThreadingIT.java - org.opennms.netmgt.telemetry.itests.ListenerParserThreadingIT (Failsafe)
        /home/jesse/git/opennms/features/telemetry/itests/src/test/java/org/opennms/netmgt/telemetry/itests/JtiIT.java - org.opennms.netmgt.telemetry.itests.JtiIT (Failsafe)
```

## Developing find-tests.py

### Running tests

```
python3 -m unittest
```

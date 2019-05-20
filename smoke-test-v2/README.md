
# Docker

The tests require Docker images to run.
 
You can pull existing images down with:
```
docker pull opennms/horizon-core-web:24.0.0-rc
docker pull opennms/minion:24.0.0-rc
docker pull opennms/sentinel:24.0.0-rc
```

And then tag them for the tests:
```
docker tag opennms/horizon-core-web:24.0.0-rc horizon
docker tag opennms/minion:24.0.0-rc minion
docker tag opennms/sentinel:24.0.0-rc sentinel
```

# Skipping teardown

Set these environment variables for your test run:
```
SKIP_CLEANUP_ON_FAILURE=true
TESTCONTAINERS_RYUK_DISABLED=true
```

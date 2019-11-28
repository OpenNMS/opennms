# Build Container images

This section describes how to build container images on your local system.
You will find the Dockerfiles in the `horizon`, `minion` and `sentinel` directories.

.Build a Horizon container image with a tag `myhorizon`
[source, shell]
===
cd horizon
docker build -t myhorizon .
===


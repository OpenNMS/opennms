# Smoke Tests

## Running locally

Download .oci artifacts

Load:
```
docker image load -i minion.oci
```

# Future Work

## Speedups

### Cache artifacts from opennms-webapp

These take a lot of resources to build, but they rarely change, let's cache them.

### Build Docker images from .tgzs

Building the packages is *slow*.
Instead of having the Docker images depend on these, can we quickly build from the .tgzs instead?
This would help us get the smoke tests started quicker.


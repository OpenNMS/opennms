# Build Container images

This section describes how to build container images on your local system.
Container images can be built with the following convenation:

1. From a tarball in `horizon/tarball` when compiled and assembled from source
2. From local DEB files in `horizon/debs`, `minion/debs`, `sentinel/debs`
3. From DEBs downloaded from the offical repositories

With running `docker build` the lookup for installation files is done in this order.

It is right now not possible to install _Minion_ and _Sentinel_ from tarballs.

## Build Container Images

```bash
cd horizon
docker build -t myhorizon .
```

```bash
cd minion
docker build -t myminion .
```

```bash
cd sentinel
docker build -t mysentinel .
```

## Customize with build arguments

The build can be customized with `--build-arg key=value`.

| Argument             | Description                                                                   | Required | Default
|:-------------------- |:------------------------------------------------------------------------------|:---------|:-----------------
| `BASE_IMAGE`         | Base image name                                                               | optional | `opennms/openjdk`
| `BASE_IMAGE_VERSION` | Version of the base image, should be the latest supported OpenJDK by default. | optional | [latest supported OpenJDK](https://hub.docker.com/r/opennms/openjdk/tags)
| `REPO_KEY_URL`       | URL for the GPG key for APT repository                                        | optional | `https://debian.opennms.org/OPENNMS-GPG-KEY`
| `ONMS_PACKAGES`      | OpenNMS packages to install. This is ignored when built from tarball.         | optional | `opennms-core opennms-webapp-jetty opennms-webapp-hawtio`
| `ADD_DEB_PACKAGES`   | If you want to add additional arbitrary deb packages                          | optional | `-`
| `CONFD_VERSION`      | Version of [confd](https://github.com/kelseyhightower/confd/releases) used to customize the configuration | optional | latest stable
| `CONFD_URL`          | Download URL for confd.                                                       | optional | `https://github.com/kelseyhightower/confd/releases/download/v${CONFD_VERSION}/confd-${CONFD_VERSION}-linux-amd64`
| `BUILD_DATE`         | Date the image is created in [RFC 3339](https://tools.ietf.org/html/rfc3339#section-5.6) format | optional | `1970-01-01T00:00:00+0000`
| `VERSION`            | Label for version number                                                      | optional | `-`
| `SOURCE`             | Label for source code URL                                                     | optional | `-`
| `REVISION`           | Descriptive/unique label for the revision                                     | optional | `-`
| `BUILD_JOB_ID`       | Label for build job from CI/CD                                                | optional | `-`
| `BUILD_NUMBER`       | Label for build number from CI/CD                                             | optional | `-`
| `BUILD_URL`          | Label for build URL from CI/CD                                                | optional | `-`
| `BUILD_BRANCH`       | Label for build branch from source repository                                 | optional | `-`


The argument for REQUIRED_DEBS are different for Horizon, Minion and Sentinel

| Argument        | Description                      | Default
|:----------------|:---------------------------------|:----------
| `REQUIRED_DEBS` | Dependency packages for Horizon  | `rrdtool jrrd2 jicmp jicmp6 r-base-core rsync libxml-twig-perl libwww-perl jq diffutils hostname sudo`
| `REQUIRED_DEBS` | Dependency packages for Sentinel | `hostname wget gettext openssh-client`

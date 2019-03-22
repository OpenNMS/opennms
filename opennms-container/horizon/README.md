# Docker Image for OpenNMS Horizon

## Version Tags

* `bleeding`, daily bleeding edge version of Horizon 24 using OpenJDK 8u191-jdk
* `23.0.0`, `latest` is a reference to last stable release of Horizon using OpenJDK 8u191-jdk

## General Project Information

* CI/CD Status: [![CircleCI](https://circleci.com/gh/opennms-forge/docker-horizon-core-web.svg?style=svg)](https://circleci.com/gh/opennms-forge/docker-horizon-core-web)
* Container Image Info: [![Container Image Info](https://images.microbadger.com/badges/version/opennms/horizon-core-web.svg)](https://microbadger.com/images/opennms/horizon-core-web "Get your own version badge on microbadger.com") [![Container Image Info](https://images.microbadger.com/badges/image/opennms/horizon-core-web.svg)](https://microbadger.com/images/opennms/horizon-core-web "Get your own image badge on microbadger.com") [![Container Image License](https://images.microbadger.com/badges/license/opennms/horizon-core-web.svg)](https://microbadger.com/images/opennms/horizon-core-web "Get your own license badge on microbadger.com") [![Anchore Image Overview](https://anchore.io/service/badges/image/84ef23ff2ddbda394e259fbab5c17d21656e837e949c39aba2579ffc3ad75756)](https://anchore.io/image/dockerhub/84ef23ff2ddbda394e259fbab5c17d21656e837e949c39aba2579ffc3ad75756?repo=opennms%2Fhorizon-core-web&tag=bleeding)
* CI/CD System: [CircleCI]
* Docker Container Image Repository: [DockerHub]
* Issue- and Bug-Tracking: [GitHub issue]
* Source code: [GitHub]

## Horizon Docker files

This repository provides snapshots for Horizon as docker images.
The image provides the Horizon core monitoring services and the web application.

It is recommended to use `docker-compose` to build a service stack using the official _PostgreSQL_ image.
In case you have already a _PostgreSQL_ database running, you can provide the database configuration in the `.opennms.env` and `.postgres.env` environment files, otherwise users and database will be created.

Data is by default persisted on your Docker host using a local volume driver for the following data:

```yml
# PostgreSQL database
psql.data:
  driver: local

# OpenNMS Horizon RRD files, logs and generated PDF reports
opennms.data:
  driver: local

# OpenNMS Horizon configuration files
opennms.etc:
  driver: local
```

It is required to manually edit _OpenNMS Horizon_ configuration files, you can add your own configuration files by providing a `etc-overlay` directory.
On startup the files overwrite the default configuration.

```yml
- ./etc-overlay:/opt/opennms-etc-overlay
```

If you prefer to have you _OpenNMS Horizon_ configuration on your Docker host in a specific directory, you can mount a directory with your config like this:

```yml
volumes:
    - ./myHorizonConfig:/opt/opennms/etc
```

In case the directory is empty, it will be initialized with a default pristine configuration from `/opt/opennms/share/etc-pristine`.

IMPORTANT: Take care about configurations which can be changed through the Web UI which are persisted on the file system, e.g. `users.xml`, `groups.xml`, `surveillance-categories.xml`, `snmp-config.xml`, etc.

## Requirements

* docker in a current stable version
* docker-compose in a current stable version
* git

## Usage

```sh
git clone https://github.com/opennms-forge/docker-horizon-core-web.git
cd docker-horizon-core-web
docker-compose up -d
```

The web application is exposed on TCP port 8980.
You can login with default user *admin* with password *admin*.
Please change immediately the default password to a secure password.

To get a help for all available container options just run:

```sh
docker run --rm opennms/horizon-core-web
```

## Running on Cassandra with Newts

By default the _OpenNMS Horizon_ image will run using _RRDTool_ for performance data storage.
However _OpenNMS Horizon_ can also be configured to run on _Cassandra_ using the _Newts_ time series schema.

The configuration options can be found in the _Environment Variables_ section.
The `opennms-cassandra-helm.yml` is provided which illustrates how to run _OpenNMS Horizon_ with a small single _Cassandra_ node on the same machine.

## Set Java Options

It is easily possible to add Java options to control the behavior of the JVM for performance tuning or debugging.
The environment variable `JAVA_OPTS` is passed on the Java command and can be used to extend or overwrite JVM options.

IMPORTANT: To give more control the Java binary command natively in the docker-entrypoint.sh and Java options in `opennms.conf` are *not* evaluated.
           The java process has PID 1 and

Used in an environment file:

```yml
env_file:
  - .java.env

cat .java.env
JAVA_OPTS=-XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintFlagsFinal
```

Used in docker-compose service environment definition:

```yml
opennms:
  container_name: opennms.core.web
  image: opennms/horizon-core-web:latest
  environment:
    - JAVA_OPTS=-XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintFlagsFinal
```

## Java and Container Heap Memory

To control and isolate resource usage of processes the Kernel feature _cgroups (Control Groups)_ is used.
With the combination of Java there are some additional things to take care of regarding the _Maximum Heap Size_ and limiting memory usage of the container.

By default [JVM ergonomics](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gc-ergonomics.html) calculates the _Maximum Heaps Size_ based on the Docker host memory and not by the memory set with with _cgroups_.

To ensure the JVM calculates the _Maximum Heap Size_ correct you have two options:

1) Set the correct _Maximum Heap Size_ manually with `-Xmx` see section above _Set Java Options_
2) If no -Xmx option is set, you can automatically calculate the _Maximum Heap Size_ with enabling the experimental cgroup aware feature with

```sh
JAVA_OPTS=-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
```

As of Java SE 8u131 the JVM is Docker-aware with Docker CPU limits transparently.
As long if `-XX:ParallelGCThreads` or `-XX:CICompilerCount` are not specified, the JVM will apply the Docker CPU limit as the number CPUs and calculates the number of GC and JIT compiler threads just like running on bare metal.

## Update and Maintenance

The entry point script is used to control starting behavior:

```sh
-f: Apply overlay configuration if exist and just start OpenNMS Horizon
-h: Show help
-i: If necessary initialize database, create pristine configuration, do an upgrade and apply the overlay config but do *not* start OpenNMS Horizon
-s: Same as -i but start OpenNMS Horizon, this should be your default
-n: Initialize Newts on Cassandra and the PostgreSQL database, do *not* start OpenNMS Horizon
-c: Same as -n but start OpenNMS Horizon, this should be your default when you want to use Newts on Cassandra
-t: Just test you configuration
```

If you want to enforce an update, create a `/opt/opennms/etc/do-upgrade` file.
Starting with `-i` or `-s` will run the `install -dis` command once to update the configuration and database schema.

All options which do upgrades or start OpenNMS Horizon verify if the configuration is valid and pass the configuration test.

## Using etc-overlay for custom configuration

If you just want to maintain custom configuration files outside of OpenNMS, you can use an etc-overlay directory.
All files in this directory are just copied into /opt/opennms/etc in the running container.
You can just mount a local directory like this:

```yml
volumes:
  - ./etc-overlay:/opt/opennms-etc-overlay
```

## Using jetty-overlay to customize Jetty WEBINF

If you just want to maintain custom configuration files for the Jetty application container, you can use an jetty-overlay directory.
All files in this directory are just copied into /opt/opennms/jetty-webapps/opennms/WEB-INF in the running container.
You can just mount a local directory like this:

```yml
volumes:
  - ./jetty-overlay:/opt/opennms-jetty-webinf-overlay
```

## Using overlay for custom arbitrary configuration

To overlay arbitrary configuration files/directory structures, you can use an opennms-overlay directory.
The contents of this directory are copied into /opt/opennms/ in the running container.
You can just mount a local directory like this:

```yml
volumes:
  - ./opennms-overlay:/opt/opennms-overlay
```

## Support and Issues

Please open issues in the [GitHub issue] section.

## Environment Variables

* `POSTGRES_HOST`: PostgreSQL database host, default: `database`
* `POSTGRES_PORT`: Port to access PostgreSQL database, default: `5432`
* `POSTGRES_USER`: PostgreSQL admin user, default: `postgres`
* `POSTGRES_PASSWORD`: PostgreSQL admin password, default: `postgres`
* `OPENNMS_DBNAME`: Database name for OpenNMS Horizon, default: `opennms`
* `OPENNMS_DBUSER`: User to access the OpenNMS Horizon database, default: `opennms`
* `OPENNMS_DBPASS`: Password for OpenNMS Horizon database user, default: `opennms`
* `OPENNMS_KARAF_SSH_HOST`: Listening address for Karaf SSH port, default: `0.0.0.0`
* `OPENNMS_KARAF_SSH_PORT`: SSH port for Karaf, default: `8101`

Using a Cassandra Cluster:

* `${OPENNMS_CASSANDRA_HOSTNAMES}`: Host name or IP address of the cassandra cluster, a comma separated list is also accepts
* `${OPENNMS_CASSANDRA_KEYSPACE}`: Name space to persist performance data in, default: `newts`
* `${OPENNMS_CASSANDRA_PORT}`: Cassandra port, default: `9042`
* `${OPENNMS_CASSANDRA_USERNAME}`: User name accessing Cassandra
* `${OPENNMS_CASSANDRA_PASSWORD}`: Password accessing Cassandra

## Build Argument

* `MIRROR_HOST`: Server with RPM packages, default: `yum.opennms.org`
* `OPENNMS_VERSION`: Version of OpenNMS Horizon RPM files, default: `stable`

[GitHub]: https://github.com/opennms-forge/docker-horizon-core-web.git
[DockerHub]: https://hub.docker.com/r/opennms/horizon-core-web
[GitHub issue]: https://github.com/opennms-forge/docker-horizon-core-web
[CircleCI]: https://circleci.com/gh/opennms-forge/docker-horizon-core-web
[Web Chat]: https://chats.opennms.org/opennms-discuss

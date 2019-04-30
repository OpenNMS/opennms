## Supported tags

* `bleeding`, daily bleeding edge version of Horizon Minion 24 using OpenJDK 8u191-jdk
* `23.0.0`, `latest` is a reference to last stable release of Horizon Minion using OpenJDK 8u191-jdk

## General Project Information

* CI/CD Status: [![CircleCI](https://circleci.com/gh/opennms-forge/docker-minion.svg?style=svg)](https://circleci.com/gh/opennms-forge/docker-minion)
* Container Image Info: [![](https://images.microbadger.com/badges/version/opennms/minion.svg)](https://microbadger.com/images/opennms/minion "Get your own version badge on microbadger.com") [![](https://images.microbadger.com/badges/image/opennms/minion.svg)](https://microbadger.com/images/opennms/minion "Get your own image badge on microbadger.com") [![](https://images.microbadger.com/badges/license/opennms/minion.svg)](https://microbadger.com/images/opennms/minion "Get your own license badge on microbadger.com")
* CI/CD System: [CircleCI]
* Docker Container Image Repository: [DockerHub]
* Issue- and Bug-Tracking: [GitHub issue]
* Source code: [GitHub]

## Minion Docker files

This repository provides OpenNMS Minions as docker images.

It is recommended to use `docker-compose` to build a service stack.
You can provide the Minion configuration in the `.minion.env` file.

## Requirements

* git
* docker
* docker-compose

## Usage

```
git clone https://github.com/opennms-forge/docker-minion.git
cd docker-minion
docker-compose up -d
```

The Karaf Shell is exposed on TCP port 8980.
Additionally the ports to receive Syslog (514/UDP) and SNMP Traps (162/UDP) are exposed as well.

To start the Minion and initialize the configuration run with argument `-f`.

You can login with default user *admin* with password *admin*.
Please change immediately the default password to a secure password described in the [Install Guide].

## Basic Environment Variables

* `MINION_ID`, the Minion ID
* `MINION_LOCATION`, the Minion Location
* `OPENNMS_HTTP_URL`, the OpenNMS WebUI Base URL
* `OPENNMS_HTTP_USER`, the user name for the OpenNMS ReST API
* `OPENNMS_HTTP_PASS`, the password for the OpenNMS ReST API
* `OPENNMS_BROKER_URL`, the ActiveMQ URL
* `OPENNMS_BROKER_USER`, the username for ActiveMQ authentication
* `OPENNMS_BROKER_PASS`, the password for ActiveMQ authentication

## Advanced Environment Variables

Kafka and UDP listeners can be configured through environment variables.
All the valid configuration entries are valid and will be processed on demand, depending on a given environment variable prefix:

* `KAFKA_RPC_`, to denote a Kafka setting for RPC
* `KAFKA_SINK_`, to denote a Kafka setting for Sink
* `UDP_`, to denote a UDP listener

### Enable Kafka for RPC (requires Horizon 23 or newer)

A sample configuration would be:

```
KAFKA_RPC_BOOTSTRAP_SERVERS=kafka_server_01:9092
KAFKA_RPC_ACKS=1
```

The above will instruct the bootstrap script to create a file called `$MINION_HOME/etc/org.opennms.core.ipc.rpc.kafka.cfg` with the following content:

```
bootstrap.servers=kafka_server_01:9092
acks=1
```

As you can see, after the prefix, you specify the name of the variable, and the underscore character will be replaced with a dot.

### Enable Kafka for Sink

A sample configuration would be:

```
KAFKA_SINK_BOOTSTRAP_SERVERS=kafka_server_01:9092
```

A similar behavior happens to populate `$MINION_HOME/etc/org.opennms.core.ipc.sink.kafka.cfg`.

### UDP Listeners

In this case, the environment variable includes the UDP port, that will be used for the configuration file name, and the properties that follow the same behavor like Kafka.
For example:

```
UDP_50001_NAME=NX-OS
UDP_50001_CLASS_NAME=org.opennms.netmgt.telemetry.listeners.udp.UdpListener
UDP_50001_LISTENER_PORT=50001
UDP_50001_HOST=0.0.0.0
UDP_50001_MAX_PACKET_SIZE=16192
```

The above will instruct the bootstrap script to create a file called `$MINION_HOME/etc/org.opennms.features.telemetry.listeners-udp-50001.cfg` with the following content:

```
name=NXOS
class-name=org.opennms.netmgt.telemetry.listeners.udp.UdpListener
listener.port=50001
maxPacketSize=16192
```

Note: `CLASS_NAME` and `MAX_PACKET_SIZE` are special cases and will be translated properly.

## Run as root or non-root

By default, Minion will run using the default `minion` user (uid: 999, gid: 997).
For this reason, if executing ICMP requests from the Minion are required, you need to specify a special kernel flag when executing `docker run`, or when using this image through `docker-compose`.
The option in question is:

```
net.ipv4.ping_group_range=0 429496729
```

For `docker run`, the syntax is:

```
docker run --sysctl "net.ipv4.ping_group_range=0 429496729" --rm --name minion -it
 -e MINION_LOCATION=Apex \
 -e OPENNMS_BROKER_URL=tcp://192.168.205.1:61616 \
 -e OPENNMS_HTTP_URL=http://192.168.205.1:8980/opennms \
 opennms/minion:bleeding -f
```

For  `docker-compose`, the syntax is:

```
version: '2.3'
services:
    minion:
        image: opennms/minion:bleeding
        environment:
          - MINION_LOCATION=Apex
          - OPENNMS_BROKER_URL=tcp://192.168.205.1:61616
          - OPENNMS_HTTP_URL=http://192.168.205.1:8980/opennms
        command: ["-f"]
        sysctls:
          - net.ipv4.ping_group_range=0 429496729
```

Another alternative to avoid providing the custom `sysctl` attribute is by running the image as root.
This can be done by passing `--user 0` to `docker run`, or by adding `user: root` on your docker-compose's yaml file.

## Dealing with Credentials

To communicate with OpenNMS credentials for the message broker and the ReST API are required.
There are two options to set those credentials to communicate with OpenNMS.

***Option 1***: Set the credentials with an environment variable

It is possible to set communication credentials with environment variables and using the `-c` option for the entrypoint.

```
docker run --rm -d \
  -e "MINION_LOCATION=Apex-Office" \
  -e "OPENNMS_BROKER_URL=tcp://172.20.11.19:61616" \
  -e "OPENNMS_HTTP_URL=http://172.20.11.19:8980/opennms" \
  -e "OPENNMS_HTTP_USER=minion" \
  -e "OPENNMS_HTTP_PASS=minion" \
  -e "OPENNMS_BROKER_USER=minion" \
  -e "OPENNMS_BROKER_PASS=minion" \
  opennms/minion -c
```

*IMPORTANT:* Be aware these credentials can be exposed in log files and the `docker inspect` command.
               It is recommended to use an encrypted keystore file which is described in option 2.

***Option 2***: Initialize and use a keystore file

Credentials for the OpenNMS communication can be stored in an encrypted keystore file `scv.jce`.
It is possible to start a Minion with a given keystore file by using a file mount into the container like `-v path/to/scv.jce:/opt/minion/etc/scv.jce`.

You can initialize a keystore file on your local system using the `-s` option on the Minion container using the interactive mode.

The following example creates a new keystore file `scv.jce` in your current working directory:

```
docker run --rm -it -v $(pwd):/keystore opennms/minion -s

Enter OpenNMS HTTP username: myminion
Enter OpenNMS HTTP password:
Enter OpenNMS Broker username: myminion
Enter OpenNMS Broker password:
[main] INFO org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault - No existing keystore found at: {}. Using empty keystore.
[main] INFO org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault - Loading existing keystore from: scv.jce
```

The keystore file can be used by mounting the file into the container and start the Minion application with `-f`.

```
docker run --rm -d \
  -e "MINION_LOCATION=Apex-Office" \
  -e "OPENNMS_BROKER_URL=tcp://172.20.11.19:61616" \
  -e "OPENNMS_HTTP_URL=http://172.20.11.19:8980/opennms" \
  -v $(pwd)/scv.jce:/opt/minion/etc/scv.jce \
  opennms/minion -f
```

## Support and Issues

Please open issues in the [GitHub issue](https://github.com/opennms-forge/docker-minion) section.

[GitHub]: https://github.com/opennms-forge/docker-minion.git
[DockerHub]: https://hub.docker.com/r/opennms/minion
[GitHub issue]: https://github.com/opennms-forge/docker-minion
[CircleCI]: https://circleci.com/gh/opennms-forge/docker-minion
[Web Chat]: https://chats.opennms.org/opennms-discuss
[IRC]: irc://freenode.org/#opennms

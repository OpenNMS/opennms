[![opennms-build](https://github.com/Bluebird-Community/opennms/actions/workflows/opennms-build.yml/badge.svg)](https://github.com/Bluebird-Community/opennms/actions/workflows/opennms-build.yml) [![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=flat-square)](https://cloudsmith.com)

This is an open-source network monitoring platform that helps you visualize and monitor everything on your local and distributed networks.
BluebirdOps offers comprehensive fault, performance, and traffic monitoring with alarm generation in one place.
Highly customizable and scalable, BluebirdOps integrates with your business applications and workflows.

## 🦄 Features

* **Full inventory management**

	Flexible provisioning system provides many ways to interoperate with configuration management systems.

* **Extensive data collection**

	Works with many industry-standard data collection protocols with no need to write or maintain third-party plugins: SNMP, JSON, WinRM, XML, SQL, JMX, SFTP, FTP, JDBC, HTTP, HTTPS, VMware, WS-Management, Prometheus.

* **Robust traffic management**

	Supports the following flow protocols: (NetFlow v5/v9, IPFIX, sFlow). 300,000+ flows/sec. BGP Monitoring support implementing the OpenBMP standards for BGP messages and metrics. Deep-dive analysis, enterprise reporting.

* **Digital experience monitoring**

	 Use the BluebirdOps Minion to monitor a service’s latency and availability from different perspectives.

* **Robust configuration**

	Configure most features through the web UI or XML scripting, including thresholding, provisioning, event and flow management, service monitoring, and performance measurement.

* **Scalability**

	Scale through Sentinels for flow persistence, Minions for Flow, BMP, SNMP trap, and Syslog ingest, and embedded ActiveMQ to Kafka message brokers.

* **Enterprise reporting and  visualization**

	Customizable dashboards that you can export as a PDF. Resource graphs, database reports, charts. Define and customize complex layered topologies to integrate topology maps into your service problem management workflow.

## 👩‍🏭 Installation

* Installing the [Core server](docs/modules/deployment/pages/core/getting-started.adoc)
* Installing a [Minion](docs/modules/deployment/pages/minion/install.adoc)
* Installing a [Sentinel](docs/modules/deployment/pages/sentinel/runtime/install.adoc)
* Setting up [Flows](docs/modules/operation/pages/deep-dive/flows/basic.adoc)

## 👩‍🔬 Build from source

Building from source requires the following components:

* OpenJDK 17 Development Kit
* Maven
* Docker if you run tests
* NodeJS 16
* NPM
* Yarn
* Docker with Docker Compose plugin

```console
git clone https://github.com/Bluebird-Community/opennms.git
cd opennms
make
```

The build artifacts are located in

* Core server: target/opennms-{pom-version}.tar.gz
* Minion: opennms-assemblies/minion/target/org.opennms.assemblies.minion-{pom-version}-minion.tar.gz
* Sentinel: opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-{pom-version}-sentinel.tar.gz

Compile and assemble the tarballs with `make`.
The tarballs are required to build the container images.

```console
cd opennms-container/core
docker build -t core .
```

```console
cd opennms-container/minion
docker build -t minion .
```

```console
cd opennms-container/sentinel
docker build -t sentinel .
```

## 🌈 Support

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that
enables your organization to create, store and share packages in any format, to any place, with total
confidence.

## Licenses

The BlueBirdOps software, as distributed here, is Copyright © 2025 by the BlueBirdOps contributors.

BlueBirdOps is a derivative work, containing original code, included code, and modified code that was published under the GNU Affero General Public License or a compatible license. Please see the source code for detailed copyright notices, but some notable copyright holders are listed below:

* The OpenNMS Horizon 34.x code base is Copyright © 2002-2025 by The OpenNMS Group, Inc.
* Source code files whose comments list other copyright holders are as indicated therein.

OpenNMS is a registered trademark of The OpenNMS Group, Inc. 

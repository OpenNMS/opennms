[OpenNMS][]
===========

[OpenNMS][] is an open-source network monitoring platform that helps you visualize and monitor everything on your local and distributed networks. OpenNMS offers comprehensive fault, performance, and traffic monitoring with alarm generation in one place. Highly customizable and scalable, OpenNMS integrates with your core business applications and workflows.


Features
---------

* **Full inventory management**

	Flexible provisioning system provides many ways to interoperate with configuration management systems.

* **Extensive data collection**

	Works with many industry-standard data collection protocols with no need to write or maintain third-party plugins: SNMP, JSON, WinRM, XML, SQL, JMX, SFTP, FTP, JDBC, HTTP, HTTPS, VMware, WS-Management, Prometheus.

* **Robust traffic management**

	Supports the following flow protocols: (NetFlow v5/v9, IPFIX, sFlow). 300,000+ flows/sec. BGP Monitoring support implementing the OpenBMP standards for BGP messages and metrics. Deep-dive analysis, enterprise reporting.

* **Digital experience monitoring**

	 Use the OpenNMS Minion to monitor a service’s latency and availability from different perspectives.

* **Robust configuration**

	Configure most features through the web UI or XML scripting, including thresholding, provisioning, event and flow management, service monitoring, and performance measurement.

* **Scalability**

	Scale through Sentinels for flow persistence, Minions for Flow, BMP, SNMP trap, and Syslog ingest, and embedded ActiveMQ to Kafka message brokers.

* **Enterprise reporting and  visualization**

	Customizable dashboards that you can export as a PDF. Resource graphs, database reports, charts. Define and customize complex layered topologies to integrate topology maps into your service problem management workflow.

Install OpenNMS
==================

For details on installing OpenNMS, see [Install OpenNMS][].

Build OpenNMS
================

For details on how to build OpenNMS, see [Build OpenNMS from source][].

[OpenNMS]:           http://www.opennms.com/
[Build OpenNMS from source]:  docs/modules/development/pages/build-from-source.adoc
[Install OpenNMS]:  docs/modules/deployment/pages/core/getting-started.adoc

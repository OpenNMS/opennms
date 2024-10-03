BlueBirdOps License
===================

This file is part of BlueBirdOps(tm).

Copyright (C) 2024 the BlueBirdOps Contributors.
Portions Copyright (C) 2002-2024 The OpenNMS Group, Inc.

BlueBirdOps is free software: you can redistribute it and/or modify it
under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your
option) any later version.

BlueBirdOps is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
for more details.

You should have received a copy of the GNU Affero General Public License
along with BlueBirdOps. If not, see <https://www.gnu.org/licenses/>.

Special Cases
=============

The following files have special cases in their licensing.  For details,
view the header in each file:

* container/bridge/src/main/java/org/opennms/container/web/bridge/internal/BridgeActivator.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/AbstractActivator.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/AbstractHttpActivator.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/DispatcherServlet.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/EventDispatcher.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/HttpServiceController.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/context/ExtServletContext.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/context/ServletContextImpl.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/context/ServletContextManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/Dispatcher.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/FilterPipeline.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/HttpFilterChain.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/InvocationFilterChain.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/NotFoundFilterChain.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/dispatch/ServletPipeline.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/AbstractHandler.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/FilterConfigImpl.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/FilterHandler.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/HandlerRegistry.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/ServletConfigImpl.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/ServletHandler.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/handler/ServletHandlerRequest.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/AbstractListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/HttpSessionAttributeListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/HttpSessionListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/ServletContextAttributeListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/ServletRequestAttributeListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/listener/ServletRequestListenerManager.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/logger/AbstractLogger.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/logger/ConsoleLogger.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/logger/LogServiceLogger.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/logger/NopLogger.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/logger/SystemLogger.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/service/DefaultHttpContext.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/service/HttpServiceFactory.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/service/HttpServiceImpl.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/service/ResourceServlet.java
* container/bridge/src/main/java/org/opennms/container/web/felix/base/internal/util/MimeTypes.java
* container/karaf/src/main/filtered-resources/etc/custom.properties
* container/servlet/src/main/java/org/opennms/container/web/DispatcherTracker.java
* container/servlet/src/main/java/org/opennms/container/web/ProxyFilter.java
* container/servlet/src/main/java/org/opennms/container/web/WebAppListener.java
* container/standalone/src/main/distribution/etc/startup.properties
* container/standalone/src/main/distribution/etc/system.properties
* container/standalone/src/main/filtered-resources/etc/custom.properties
* core/snmp/impl-snmp4j/src/main/java/org/opennms/netmgt/snmp/snmp4j/Integer32IgnoreTooManyBytes.java
* core/soa/src/main/java/org/opennms/core/soa/support/OnmsOSGiBridgeActivator.java
* core/test-api/dns/src/main/java/org/opennms/core/test/dns/DNSServer.java
* features/nrtg/config.properties
* integrations/opennms-vmware/src/main/java/org/opennms/netmgt/collectd/VmwareCimCollector.java
* integrations/opennms-vmware/src/main/java/org/opennms/netmgt/poller/monitors/VmwareCimMonitor.java
* integrations/opennms-vmware/src/main/java/org/opennms/protocols/vmware/VmwareViJavaAccess.java
* integrations/opennms-vmware/src/test/java/org/opennms/netmgt/collectd/vmware/VmwareViJavaAccessTest.java
* maven/conf/logging/simplelogger.properties
* opennms-services/src/main/java/org/opennms/netmgt/xmlrpcd/TimeoutSecureXmlRpcClient.java
* opennms-services/src/main/java/org/opennms/netmgt/xmlrpcd/TimeoutSecureXmlRpcTransport.java
* opennms-services/src/main/java/org/opennms/netmgt/xmlrpcd/TimeoutSecureXmlRpcTransportFactory.java
* opennms-services/src/test/java/org/opennms/netmgt/syslogd/SyslogClient.java


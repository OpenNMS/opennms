/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfig;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.actiond.ActiondConfiguration;
import org.opennms.netmgt.config.ami.AmiConfig;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.javamail.JavamailConfiguration;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.mailtransporttest.MailTransportTest;
import org.opennms.netmgt.config.map.adapter.MapsAdapterConfiguration;
import org.opennms.netmgt.config.microblog.MicroblogConfiguration;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.rancid.adapter.RancidConfiguration;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.config.reporting.opennms.OpennmsReports;
import org.opennms.netmgt.config.rtc.RTCConfiguration;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.scriptd.ScriptdConfiguration;
import org.opennms.netmgt.config.server.LocalServer;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration;
import org.opennms.netmgt.config.snmpinterfacepoller.SnmpInterfacePollerConfiguration;
import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.syslogd.SyslogdConfiguration;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.config.tl1d.Tl1dConfiguration;
import org.opennms.netmgt.config.translator.EventTranslatorConfiguration;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.config.viewsdisplay.Viewinfo;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.config.vmware.vijava.VmwareDatacollectionConfig;
import org.opennms.netmgt.config.wmi.WmiConfig;
import org.opennms.netmgt.config.wmi.WmiDatacollectionConfig;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class Normalizer {
	public static enum Implementation {
		JAXB,
		CASTOR
	}
	
	public static class Source {
		public File configFile;
		public Class<?> clazz;
		public Implementation implementation;
		public Source(File configFile, Class<?> clazz, Implementation implementation) {
			this.configFile = configFile;
			this.clazz = clazz;
			this.implementation = implementation;
		}
	}

	public static void main(String[] args) {
		try {
			new Normalizer().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File configHome;
	private ArrayList<Source> configSources = new ArrayList<Source>();

	private void addFile(final String fileName, final Class<?> clazz, final Implementation implementation) {
		File file = new File(configHome, fileName);
		if (file.exists()) {
			configSources.add(new Source(file, clazz, implementation));			
		} else {
			System.out.println("Not Found : " + file);
		}
	}

	public Normalizer() throws IOException {
		configHome = new File(System.getProperty("opennms.home", "/opt/opennms"), "etc");
		System.out.println("Normalizing OpenNMS configuration files at " + configHome);

		LocalConfiguration.getInstance().getProperties().clear();
        LocalConfiguration.getInstance().getProperties().load(getClass().getResourceAsStream("/castor.properties"));

		addFile("actiond-configuration.xml", ActiondConfiguration.class, Implementation.JAXB);
		addFile("ami-config.xml", AmiConfig.class, Implementation.JAXB);
		addFile("availability-reports.xml", OpennmsReports.class, Implementation.CASTOR);
		addFile("capsd-configuration.xml", CapsdConfiguration.class, Implementation.CASTOR);
		addFile("examples/capsd-configuration.xml", CapsdConfiguration.class, Implementation.CASTOR);
		addFile("categories.xml", Catinfo.class, Implementation.CASTOR);
		addFile("chart-configuration.xml", ChartConfiguration.class, Implementation.CASTOR);
		addFile("collectd-configuration.xml", CollectdConfiguration.class, Implementation.JAXB);
		addFile("examples/collectd-configuration.xml", CollectdConfiguration.class, Implementation.JAXB);
		addFile("database-reports.xml", LegacyLocalReportsDefinition.class, Implementation.JAXB); // There are issues on the implementation of LegacyLocalReportsDefinition that must be fixed.
		addFile("database-schema.xml", DatabaseSchema.class, Implementation.CASTOR);
		addFile("datacollection-config.xml", DatacollectionConfig.class, Implementation.JAXB);
		addFile("examples/old-datacollection-config.xml", DatacollectionConfig.class, Implementation.JAXB);
		addFile("destinationPaths.xml", DestinationPaths.class, Implementation.CASTOR);
		addFile("examples/destinationPaths.xml", DestinationPaths.class, Implementation.CASTOR);
		addFile("discovery-configuration.xml", DiscoveryConfiguration.class, Implementation.CASTOR);
		addFile("examples/discovery-configuration.xml", DiscoveryConfiguration.class, Implementation.CASTOR);
		addFile("eventconf.xml", Events.class, Implementation.JAXB);
		addFile("groups.xml", Groupinfo.class, Implementation.CASTOR);
		addFile("examples/groups.xml", Groupinfo.class, Implementation.CASTOR);
		addFile("http-datacollection-config.xml", HttpDatacollectionConfig.class, Implementation.CASTOR);
		addFile("examples/devices/motorola_cpei_150_wimax_gateway/http-datacollection-config.xml", HttpDatacollectionConfig.class, Implementation.CASTOR);
		addFile("jasper-reports.xml", LocalJasperReports.class, Implementation.JAXB);
		addFile("jmx-datacollection-config.xml", JmxDatacollectionConfig.class, Implementation.JAXB);
		addFile("ksc-performance-reports.xml", ReportsList.class, Implementation.CASTOR);
		addFile("linkd-configuration.xml", LinkdConfiguration.class, Implementation.CASTOR);
		addFile("enlinkd-configuration.xml", EnlinkdConfiguration.class, Implementation.CASTOR);
		addFile("examples/linkd-configuration.xml", LinkdConfiguration.class, Implementation.CASTOR);
		addFile("examples/mail-transport-test.xml", MailTransportTest.class, Implementation.JAXB);
		addFile("examples/hyperic-integration/imports-HQ.xml", Requisition.class, Implementation.JAXB);
		addFile("examples/hyperic-integration/imports-opennms-admin.xml", Requisition.class, Implementation.JAXB);
		addFile("monitoring-locations.xml", MonitoringLocationsConfiguration.class, Implementation.JAXB);
		addFile("examples/monitoring-locations.xml", MonitoringLocationsConfiguration.class, Implementation.JAXB);
		addFile("notifd-configuration.xml", NotifdConfiguration.class, Implementation.CASTOR);
		addFile("notificationCommands.xml", NotificationCommands.class, Implementation.CASTOR);
		addFile("examples/notificationCommands.xml", NotificationCommands.class, Implementation.CASTOR);
		addFile("notifications.xml", Notifications.class, Implementation.CASTOR);
		addFile("examples/notifications.xml", Notifications.class, Implementation.CASTOR);
		addFile("opennms-datasources.xml", DataSourceConfiguration.class, Implementation.CASTOR);
		addFile("opennms-server.xml", LocalServer.class, Implementation.JAXB);
		addFile("examples/opennms-server.xml", LocalServer.class, Implementation.JAXB);
		addFile("poll-outages.xml", Outages.class, Implementation.JAXB);
		addFile("examples/poll-outages.xml", Outages.class, Implementation.JAXB);
		addFile("poller-configuration.xml", PollerConfiguration.class, Implementation.JAXB);
		addFile("examples/poller-configuration.xml", PollerConfiguration.class, Implementation.JAXB);
		addFile("rtc-configuration.xml", RTCConfiguration.class, Implementation.CASTOR);
		addFile("scriptd-configuration.xml", ScriptdConfiguration.class, Implementation.CASTOR);
		addFile("syslog-northbounder-configuration.xml", SyslogNorthbounderConfig.class, Implementation.JAXB);
		addFile("examples/scriptd-configuration.xml", ScriptdConfiguration.class, Implementation.CASTOR);
		addFile("examples/event-proxy/Proxy.events.xml", Events.class, Implementation.JAXB);
		addFile("examples/event-proxy/scriptd-configuration.xml", ScriptdConfiguration.class, Implementation.CASTOR);
		addFile("examples/event-proxy/vacuumd-configuration.xml", VacuumdConfiguration.class, Implementation.JAXB);
		addFile("site-status-views.xml", SiteStatusViewConfiguration.class, Implementation.CASTOR);
		addFile("snmp-config.xml", SnmpConfig.class, Implementation.JAXB);
		addFile("examples/snmp-config.xml", SnmpConfig.class, Implementation.JAXB);
		addFile("snmp-interface-poller-configuration.xml", SnmpInterfacePollerConfiguration.class, Implementation.CASTOR);
		addFile("statsd-configuration.xml", StatisticsDaemonConfiguration.class, Implementation.CASTOR);
		addFile("surveillance-views.xml", SurveillanceViewConfiguration.class, Implementation.CASTOR);
		addFile("examples/surveillance-views.xml", SurveillanceViewConfiguration.class, Implementation.CASTOR);
		addFile("syslogd-configuration.xml", SyslogdConfiguration.class, Implementation.CASTOR);
		addFile("threshd-configuration.xml", ThreshdConfiguration.class, Implementation.CASTOR);
		addFile("examples/threshd-configuration.xml", ThreshdConfiguration.class, Implementation.CASTOR);
		addFile("thresholds.xml", ThresholdingConfig.class, Implementation.CASTOR);
		addFile("examples/thresholds.xml", ThresholdingConfig.class, Implementation.CASTOR);
		addFile("tl1d-configuration.xml", Tl1dConfiguration.class, Implementation.CASTOR);
		addFile("translator-configuration.xml", EventTranslatorConfiguration.class, Implementation.CASTOR);
		addFile("trapd-configuration.xml", TrapdConfiguration.class, Implementation.CASTOR);
		addFile("users.xml", Userinfo.class, Implementation.CASTOR);
		addFile("vacuumd-configuration.xml", VacuumdConfiguration.class, Implementation.JAXB);
		addFile("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class, Implementation.CASTOR);
		addFile("examples/xmlrpcd-configuration.xml", XmlrpcdConfiguration.class, Implementation.CASTOR);
		addFile("eventd-configuration.xml", EventdConfiguration.class, Implementation.CASTOR);
		addFile("service-configuration.xml", ServiceConfiguration.class, Implementation.JAXB);
		addFile("viewsdisplay.xml", Viewinfo.class, Implementation.CASTOR);
		addFile("examples/viewsdisplay.xml", Viewinfo.class, Implementation.CASTOR);
		addFile("examples/tl1d-configuration.xml", Tl1dConfiguration.class, Implementation.CASTOR);
		addFile("wmi-config.xml", WmiConfig.class, Implementation.CASTOR);
		addFile("wmi-datacollection-config.xml", WmiDatacollectionConfig.class, Implementation.CASTOR);
		addFile("javamail-configuration.xml", JavamailConfiguration.class, Implementation.CASTOR);
		addFile("ackd-configuration.xml", AckdConfiguration.class, Implementation.JAXB);
		addFile("provisiond-configuration.xml", ProvisiondConfiguration.class, Implementation.CASTOR);
		addFile("reportd-configuration.xml", ReportdConfiguration.class, Implementation.CASTOR);
		addFile("rws-configuration.xml", RwsConfiguration.class, Implementation.CASTOR);
		addFile("examples/rws-configuration.xml", RwsConfiguration.class, Implementation.CASTOR);
		addFile("mapsadapter-configuration.xml", MapsAdapterConfiguration.class, Implementation.CASTOR);
		addFile("examples/mapsadapter-configuration.xml", MapsAdapterConfiguration.class, Implementation.CASTOR);
		addFile("rancid-configuration.xml", RancidConfiguration.class, Implementation.CASTOR);
		addFile("examples/rancid-configuration.xml", RancidConfiguration.class, Implementation.CASTOR);
		addFile("microblog-configuration.xml", MicroblogConfiguration.class, Implementation.CASTOR);
		addFile("snmp-asset-adapter-configuration.xml", SnmpAssetAdapterConfiguration.class, Implementation.CASTOR);
		addFile("jdbc-datacollection-config.xml", JdbcDataCollectionConfig.class, Implementation.JAXB);
		addFile("remote-repository.xml", RemoteRepositoryConfig.class, Implementation.JAXB);
		addFile("vmware-config.xml", VmwareConfig.class, Implementation.JAXB);
		addFile("vmware-datacollection-config.xml", VmwareDatacollectionConfig.class, Implementation.JAXB);
		addFile("vmware-cim-datacollection-config.xml", VmwareCimDatacollectionConfig.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/collectd-configuration.xml", CollectdConfiguration.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection-config.xml", JmxDatacollectionConfig.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/ActiveMQ/5.6/ActiveMQBasic0.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/Cassandra/1.1.2/CassandraBasic0.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/JBoss/4/JBossBasic0.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/Jvm/1.6/JvmBasic0.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/Jvm/1.6/JvmLegacy.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/OpenNMS/1.10/OpenNMSBasic0.xml", Mbeans.class, Implementation.JAXB);
		addFile("examples/jvm-datacollection/jmx-datacollection/OpenNMS/1.10/OpenNMSLegacy.xml", Mbeans.class, Implementation.JAXB);
		addFile("snmp-hardware-inventory-adapter-configuration.xml", HwInventoryAdapterConfiguration.class, Implementation.JAXB);

		for (final File file : FileUtils.listFiles(new File(configHome, "events"), new String[] { "xml" }, true)) {
			addFile("events/" + file.getName(), Events.class, Implementation.JAXB);
		}
		for (final File file : FileUtils.listFiles(new File(configHome, "datacollection"), new String[] { "xml" }, true)) {
			addFile("datacollection/" + file.getName(), DatacollectionGroup.class, Implementation.JAXB);
		}
	}

	public void normalize() throws Exception {
		for (Source source : configSources) {
			unmarshall(source);
		}
	}

	public void unmarshall(Source source) {
		try {
			System.out.println("Normalizing " + source.configFile);
			final Resource resource = new FileSystemResource(source.configFile);
			Object result = null;
			switch (source.implementation) {
			case CASTOR:
				result = CastorUtils.unmarshal(source.clazz, resource);
				Marshaller.marshal(result, new FileWriter(source.configFile));
				result = CastorUtils.unmarshal(source.clazz, resource); // Double check the newly generated file can be processed correctly.
				break;

			case JAXB:
				result = JaxbUtils.unmarshal(source.clazz, resource);
				JaxbUtils.marshal(result, new FileWriter(source.configFile));
				result = JaxbUtils.unmarshal(source.clazz, resource); // Double check the newly generated file can be processed correctly.
				break;
			}
			if (result == null) {
				throw new IllegalArgumentException("Invalid implementation.");
			}
		} catch(Exception ex) {
			System.err.println("Cannot normalize file: " + ex);
		}
	}
}

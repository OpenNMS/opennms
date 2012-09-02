/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


package org.opennms.netmgt.config;

import junit.framework.AssertionFailedError;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.actiond.ActiondConfiguration;
import org.opennms.netmgt.config.ami.AmiConfig;
import org.opennms.netmgt.config.archiver.events.EventsArchiverConfiguration;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.eventd.EventdConfiguration;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.groups.Groupinfo;
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
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.config.poller.PollerConfiguration;
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
import org.opennms.netmgt.config.vulnscand.VulnscandConfiguration;
import org.opennms.netmgt.config.wmi.WmiConfig;
import org.opennms.netmgt.config.wmi.WmiDatacollectionConfig;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * The name of this class is a tribute to
 * <a href="http://www.willitblend.com/">www.willitblend.com</a>.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class WillItUnmarshalTest {
    private static final String CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY = "org.exolab.castor.xml.lenient.sequence.order";
    private static Set<String> m_filesTested = new HashSet<String>();
    private static Set<String> m_exampleFilesTested = new HashSet<String>();
    
    @Before
    public void setUp() throws Exception {
        
        MockLogAppender.setupLogging(true, "INFO");
        
        // Reload castor properties every time since some tests fiddle with them
        LocalConfiguration.getInstance().getProperties().clear();
        LocalConfiguration.getInstance().getProperties().load(ConfigurationTestUtils.getInputStreamForResource(this, "/castor.properties"));
    }

    @After
    public void checkWarnings() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testGoodOrdering() throws Exception {
        Resource resource = ConfigurationTestUtils.getSpringResourceForResource(this, "eventconf-good-ordering.xml");
        System.out.println("Unmarshalling: " + resource.getURI());
        JaxbUtils.unmarshal(Events.class, resource);
    }

    @Test
    public void testLenientOrdering() throws Exception {
        Resource resource = ConfigurationTestUtils.getSpringResourceForResource(this, "eventconf-bad-ordering.xml");
        System.out.println("Unmarshalling: " + resource.getURI());
        JaxbUtils.unmarshal(Events.class, resource);
    }
    
    @Test
    public void testUpdateFields() throws Exception {
        Resource resource = ConfigurationTestUtils.getSpringResourceForResource(this, "eventconf-update-fields.xml");
        System.out.println("Unmarshalling: " + resource.getURI());
        JaxbUtils.unmarshal(Events.class, resource);
    }


    @Test
    public void testFailOnInvalidElement() throws Exception {
        unmarshalAndAnticipateException("eventconf-bad-element.xml", "Invalid content was found starting with element 'bad-element'.");
    }

    @Test
    public void testActiondConfiguration() throws Exception {
        unmarshal("actiond-configuration.xml", ActiondConfiguration.class);
    }
    @Test
    public void testAmiConfig() throws Exception {
        unmarshal("ami-config.xml", AmiConfig.class);
    }
    @Test
    public void testAvailabilityReportsConfiguration() throws Exception {
        unmarshal("availability-reports.xml", OpennmsReports.class);
    }
    @Test
    public void testCapsdConfiguration() throws Exception {
        unmarshal("capsd-configuration.xml", CapsdConfiguration.class);
    }
    @Test
    public void testExampleCapsdConfiguration() throws Exception {
        unmarshalExample("capsd-configuration.xml", CapsdConfiguration.class);
    }
    @Test
    public void testCategories() throws Exception {
        unmarshal("categories.xml", Catinfo.class);
    }
    @Test
    public void testChartConfiguration() throws Exception {
        unmarshal("chart-configuration.xml", ChartConfiguration.class);
    }
    @Test
    public void testCollectdConfiguration() throws Exception {
        unmarshal("collectd-configuration.xml", CollectdConfiguration.class);
    }
    @Test
    public void testExampleCollectdConfiguration() throws Exception {
        unmarshalExample("collectd-configuration.xml", CollectdConfiguration.class);
    }
    @Test
    public void testDatabaseReportsConfiguration() throws Exception {
        unmarshalJaxb("database-reports.xml", LegacyLocalReportsDefinition.class);
    }
    @Test
    public void testDatabaseSchema() throws Exception {
        unmarshal("database-schema.xml", DatabaseSchema.class);
    }
    @Test
    public void testDataCollectionConfiguration() throws Exception {
        unmarshalJaxb("datacollection-config.xml", DatacollectionConfig.class);
    }
    @Test
    public void testExampleOldDataCollectionConfiguration() throws Exception {
        unmarshalJaxbExample("old-datacollection-config.xml", DatacollectionConfig.class);
    }
    @Test
    public void testDestinationPaths() throws Exception {
        unmarshal("destinationPaths.xml", DestinationPaths.class);
    }
    @Test
    public void testExampleDestinationPaths() throws Exception {
        unmarshalExample("destinationPaths.xml", DestinationPaths.class);
    }
    @Test
    public void testDiscoveryConfiguration() throws Exception {
        unmarshal("discovery-configuration.xml", DiscoveryConfiguration.class);
    }
    @Test
    public void testExampleDiscoveryConfiguration() throws Exception {
        unmarshalExample("discovery-configuration.xml", DiscoveryConfiguration.class);
    }
    @Test
    public void testEventconf() throws Exception {
        unmarshal("eventconf.xml", Events.class);
    }
    @Test
    public void testEventsArchiverConfiguration() throws Exception {
        unmarshal("events-archiver-configuration.xml", EventsArchiverConfiguration.class);
    }
    @Test
    public void testGroups() throws Exception {
        unmarshal("groups.xml", Groupinfo.class);
    }
    @Test
    public void testExampleGroups() throws Exception {
        unmarshalExample("groups.xml", Groupinfo.class);
    }
    @Test
    public void testHttpDataCollectionConfiguration() throws Exception {
        unmarshal("http-datacollection-config.xml", HttpDatacollectionConfig.class);
    }
    @Test
    public void testExampleHttpDataCollectionConfiguration() throws Exception {
        unmarshalExample("devices/motorola_cpei_150_wimax_gateway/http-datacollection-config.xml", HttpDatacollectionConfig.class);
    }
    @Test
    public void testJasperReportsConfiguration() throws Exception {
        unmarshalJaxb("jasper-reports.xml", LocalJasperReports.class);
    }
    @Test
    public void testJmxDataCollectionConfiguration() throws Exception {
        unmarshal("jmx-datacollection-config.xml", JmxDatacollectionConfig.class);
    }
    @Test
    public void testKscPerformanceReports() throws Exception {
        unmarshal("ksc-performance-reports.xml", ReportsList.class);
    }
    @Test
    public void testLinkdConfiguration() throws Exception {
        unmarshal("linkd-configuration.xml", LinkdConfiguration.class);
    }
    @Test
    public void testExampleMailTransportTest() throws Exception {
        unmarshalExample("mail-transport-test.xml", MailTransportTest.class);
    }
    @Test
    public void testExampleHypericImportsHQ() throws Exception {
        unmarshalJaxbExample("hyperic-integration/imports-HQ.xml", Requisition.class);
    }
    @Test
    public void testExampleHypericImportsOpennmsAdmin() throws Exception {
        unmarshalJaxbExample("hyperic-integration/imports-opennms-admin.xml", Requisition.class);
    }
    @Test
    public void testMonitoringLocations() throws Exception {
        unmarshal("monitoring-locations.xml", MonitoringLocationsConfiguration.class);
    }
    @Test
    public void testExampleMonitoringLocations() throws Exception {
        unmarshalExample("monitoring-locations.xml", MonitoringLocationsConfiguration.class);
    }
    @Test
    public void testNotifdConfiguration() throws Exception {
        unmarshal("notifd-configuration.xml", NotifdConfiguration.class);
    }
    @Test
    public void testNotificationCommands() throws Exception {
        unmarshal("notificationCommands.xml", NotificationCommands.class);
    }
    @Test
    public void testExampleNotificationCommands() throws Exception {
        unmarshalExample("notificationCommands.xml", NotificationCommands.class);
    }
    @Test
    public void testNotifications() throws Exception {
        unmarshal("notifications.xml", Notifications.class);
    }
    @Test
    public void testExampleNotifications() throws Exception {
        unmarshalExample("notifications.xml", Notifications.class);
    }
    @Test
    public void testOpennmsDatasources() throws Exception {
        unmarshal("opennms-datasources.xml", DataSourceConfiguration.class);
    }
    @Test
    public void testOpennmsServer() throws Exception {
        unmarshal("opennms-server.xml", LocalServer.class);
    }
    @Test
    public void testExampleOpennmsServer() throws Exception {
        unmarshalExample("opennms-server.xml", LocalServer.class);
    }
    @Test
    public void testPollOutages() throws Exception {
        unmarshal("poll-outages.xml", Outages.class);
    }
    @Test
    public void testExamplePollOutages() throws Exception {
        unmarshalExample("poll-outages.xml", Outages.class);
    }
    @Test
    public void testPollerConfiguration() throws Exception {
        unmarshal("poller-configuration.xml", PollerConfiguration.class);
    }
    @Test
    public void testExamplePollerConfiguration() throws Exception {
        unmarshalExample("poller-configuration.xml", PollerConfiguration.class);
    }
    @Test
    public void testRtcConfiguration() throws Exception {
        unmarshal("rtc-configuration.xml", RTCConfiguration.class);
    }
    @Test
    public void testScriptdConfiguration() throws Exception {
        unmarshal("scriptd-configuration.xml", ScriptdConfiguration.class);
    }
    @Test
    public void testExampleScriptdConfiguration() throws Exception {
        unmarshalExample("scriptd-configuration.xml", ScriptdConfiguration.class);
    }
    @Test
    public void testExampleEventProxyProxyEvents() throws Exception {
        unmarshalExample("event-proxy/Proxy.events.xml", Events.class);
    }
    @Test
    public void testExampleEventProxyScriptdConfiguration() throws Exception {
        unmarshalExample("event-proxy/scriptd-configuration.xml", ScriptdConfiguration.class);
    }
    @Test
    public void testExampleEventProxyVacuumdConfiguration() throws Exception {
        unmarshalExample("event-proxy/vacuumd-configuration.xml", VacuumdConfiguration.class);
    }
    @Test
    public void testSiteStatusViews() throws Exception {
        unmarshal("site-status-views.xml", SiteStatusViewConfiguration.class);
    }
    @Test
    public void testSnmpConfig() throws Exception {
        unmarshalJaxb("snmp-config.xml", SnmpConfig.class);
    }
    @Test
    public void testExampleSnmpConfig() throws Exception {
        unmarshalJaxbExample("snmp-config.xml", SnmpConfig.class);
    }
    @Test
    public void testSnmpInterfacePollerConfiguration() throws Exception {
        unmarshal("snmp-interface-poller-configuration.xml", SnmpInterfacePollerConfiguration.class);
    }
    @Test
    public void testStatsdConfiguration() throws Exception {
        unmarshal("statsd-configuration.xml", StatisticsDaemonConfiguration.class);
    }
    @Test
    public void testSurveillanceViews() throws Exception {
        unmarshal("surveillance-views.xml", SurveillanceViewConfiguration.class);
    }
    @Test
    public void testExampleSurveillanceViews() throws Exception {
        unmarshalExample("surveillance-views.xml", SurveillanceViewConfiguration.class);
    }
    @Test
    public void testSyslogdConfiguration() throws Exception {
        unmarshal("syslogd-configuration.xml", SyslogdConfiguration.class);
    }
    @Test
    public void testThreshdConfiguration() throws Exception {
        unmarshal("threshd-configuration.xml", ThreshdConfiguration.class);
    }
    @Test
    public void testExampleThreshdConfiguration() throws Exception {
        unmarshalExample("threshd-configuration.xml", ThreshdConfiguration.class);
    }
    @Test
    public void testThresholds() throws Exception {
        unmarshal("thresholds.xml", ThresholdingConfig.class);
    }
    @Test
    public void testExampleThresholds() throws Exception {
        unmarshalExample("thresholds.xml", ThresholdingConfig.class);
    }
    @Test
    public void testTl1dConfiguration() throws Exception {
        unmarshal("tl1d-configuration.xml", Tl1dConfiguration.class);
    }
    @Test
    public void testTranslatorConfiguration() throws Exception {
        unmarshal("translator-configuration.xml", EventTranslatorConfiguration.class);
    }
    @Test
    public void testTrapdonfiguration() throws Exception {
        unmarshal("trapd-configuration.xml", TrapdConfiguration.class);
    }
    @Test
    public void testUsers() throws Exception {
        unmarshal("users.xml", Userinfo.class);
    }
    @Test
    public void testVacuumdConfiguration() throws Exception {
        unmarshal("vacuumd-configuration.xml", VacuumdConfiguration.class);
    }
    @Test
    public void testVulnscandConfiguration() throws Exception {
        unmarshal("vulnscand-configuration.xml", VulnscandConfiguration.class);
    }
    @Test
    public void testXmlrpcdConfiguration() throws Exception {
        unmarshal("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class);
    }
    @Test
    public void testExampleXmlrpcdConfiguration() throws Exception {
        unmarshalExample("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class);
    }
    @Test
    public void testEventdConfiguration() throws Exception {
        unmarshal("eventd-configuration.xml", EventdConfiguration.class);
    }
    @Test
    public void testServiceConfiguration() throws Exception {
        unmarshal("service-configuration.xml", ServiceConfiguration.class);
    }
    @Test
    public void testViewsDisplay() throws Exception {
        unmarshal("viewsdisplay.xml", Viewinfo.class);
    }
    @Test
    public void testExampleViewsDisplay() throws Exception {
        unmarshalExample("viewsdisplay.xml", Viewinfo.class);
    }
    @Test
    public void testExampleTl1dConfiguration() throws Exception {
        unmarshalExample("tl1d-configuration.xml", Tl1dConfiguration.class);
    }
    @Test
    public void testWmiConfiguration() throws Exception {
        unmarshal("wmi-config.xml", WmiConfig.class);
    }
    @Test
    public void testWmiDatacollectionConfiguration() throws Exception {
        unmarshal("wmi-datacollection-config.xml", WmiDatacollectionConfig.class);
    }
    @Test
    public void testJavaMailConfiguration() throws Exception {
        unmarshal("javamail-configuration.xml", JavamailConfiguration.class);
    }
    @Test
    public void testAckdConfiguration() throws Exception {
        unmarshal("ackd-configuration.xml", AckdConfiguration.class);
    }
    @Test
    public void provisiondConfiguration() throws Exception {
        unmarshal("provisiond-configuration.xml", ProvisiondConfiguration.class);
    }
    @Test
    public void testReportdConfiguration() throws Exception {
        unmarshal("reportd-configuration.xml", ReportdConfiguration.class);
    }
    @Test
    public void testRwsConfiguration() throws Exception {
        unmarshal("rws-configuration.xml", RwsConfiguration.class);
    }
    @Test
    public void testExampleRwsConfiguration() throws Exception {
        unmarshalExample("rws-configuration.xml", RwsConfiguration.class);
    }
    @Test
    public void testMapsAdapterConfiguration() throws Exception {
        unmarshal("mapsadapter-configuration.xml", MapsAdapterConfiguration.class);
    }
    @Test
    public void testExampleMapsAdapterConfiguration() throws Exception {
        unmarshalExample("mapsadapter-configuration.xml", MapsAdapterConfiguration.class);
    }
    @Test
    public void testRancidAdapterConfiguration() throws Exception {
        unmarshal("rancid-configuration.xml", RancidConfiguration.class);
    }
    @Test
    public void testExampleRancidAdapterConfiguration() throws Exception {
        unmarshalExample("rancid-configuration.xml", RancidConfiguration.class);
    }
    @Test
    public void testMicroblogConfiguration() throws Exception {
        unmarshal("microblog-configuration.xml", MicroblogConfiguration.class);
    }
    @Test
    public void testSnmpAssetAdapterConfiguration() throws Exception {
        unmarshal("snmp-asset-adapter-configuration.xml", SnmpAssetAdapterConfiguration.class);
    }
    @Test
    public void testJdbcDataCollectionConfiguration() throws Exception {
        unmarshalJaxb("jdbc-datacollection-config.xml", JdbcDataCollectionConfig.class);
    }

    @Test
    public void testRemoteRepositoryXmlConfiguration() throws Exception {
        unmarshalJaxb("remote-repository.xml", RemoteRepositoryConfig.class);
    }

    @Test
    public void testCheckAllDaemonXmlConfigFilesTested() {
        File someConfigFile = ConfigurationTestUtils.getFileForConfigFile("discovery-configuration.xml");
        File configDir = someConfigFile.getParentFile();
        assertTrue("daemon configuration directory exists at " + configDir.getAbsolutePath(), configDir.exists());
        assertTrue("daemon configuration directory is a directory at " + configDir.getAbsolutePath(), configDir.isDirectory());

        String[] configFiles = configDir.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });
        
        Set<String> allXml = new HashSet<String>(Arrays.asList(configFiles));
        
        allXml.removeAll(m_filesTested);
        
        if (allXml.size() > 0) {
            List<String> files = new ArrayList<String>(allXml);
            Collections.sort(files);
            fail("These files in " + configDir.getAbsolutePath() + " were not tested: \n\t" + StringUtils.collectionToDelimitedString(files, "\n\t"));
        }
    }
    
    @Test
    public void testCheckAllDaemonXmlExampleConfigFilesTested() {
        File someConfigFile = ConfigurationTestUtils.getFileForConfigFile("discovery-configuration.xml");
        File examplesDir = new File(someConfigFile.getParentFile(), "examples");

        Set<String> allXml = new HashSet<String>();
        findConfigurationFilesInDirectory(examplesDir, null, allXml);
        
        allXml.removeAll(m_exampleFilesTested);
        allXml.remove("correlation-engine.xml");
        allXml.remove("drools-engine.xml");
        allXml.remove("nodeParentRules-context.xml");
        allXml.remove("nsclient-config.xml");
        if (allXml.size() > 0) {
            List<String> files = new ArrayList<String>(allXml);
            Collections.sort(files);
            fail("These files in " + examplesDir.getAbsolutePath() + " were not tested: \n\t" + StringUtils.collectionToDelimitedString(files, "\n\t"));
        }
    }

    private void findConfigurationFilesInDirectory(File directory, String directoryPrefix, Set<String> allXml) {
        assertTrue("directory to search for configuration files in is not a directory at " + directory.getAbsolutePath(), directory.isDirectory());

        String[] configFiles = directory.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });
        for (String configFile : configFiles) {
            allXml.add((directoryPrefix != null) ? (directoryPrefix + File.separator + configFile) : configFile);
        }
        
        File[] subDirectories = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().startsWith(".");
            }
        });
        
        for (File subDirectory : subDirectories) {
            String newPrefix = (directoryPrefix != null) ? (directoryPrefix + File.separator + subDirectory.getName()) : subDirectory.getName();
            findConfigurationFilesInDirectory(subDirectory, newPrefix, allXml);
        }
    }
    
    @Test
    public void testAllIncludedEventXml() throws Exception {
        File eventConfFile = ConfigurationTestUtils.getFileForConfigFile("eventconf.xml");
        File eventsDirFile = new File(eventConfFile.getParentFile(), "events");
        assertTrue("events directory exists at " + eventsDirFile.getAbsolutePath(), eventsDirFile.exists());
        assertTrue("events directory is a directory at " + eventsDirFile.getAbsolutePath(), eventsDirFile.isDirectory());
        
        File[] includedEventFiles = eventsDirFile.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });
        
        for (File includedEventFile : includedEventFiles) {
            try {
                // Be conservative about what we ship, so don't be lenient
                LocalConfiguration.getInstance().getProperties().remove(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY);
                Resource resource = new FileSystemResource(includedEventFile);
                System.out.println("Unmarshalling: " + resource.getURI());
                CastorUtils.unmarshal(Events.class, resource);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to unmarshal " + includedEventFile + ": " + t, t);
            }
        }
    }

    @Test
    public void testAllIncludedDatacollectionGroups() throws Exception {
        File dataCollectionConfFile = ConfigurationTestUtils.getFileForConfigFile("datacollection-config.xml");
        File groupDirFile = new File(dataCollectionConfFile.getParentFile(), "datacollection");
        assertTrue("events directory exists at " + groupDirFile.getAbsolutePath(), groupDirFile.exists());
        assertTrue("events directory is a directory at " + groupDirFile.getAbsolutePath(), groupDirFile.isDirectory());

        File[] includedGroupFiles = groupDirFile.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            } });

        for (File includedGroupFile : includedGroupFiles) {
            try {
                // Be conservative about what we ship, so don't be lenient
                Resource resource = new FileSystemResource(includedGroupFile);
                System.out.println("Unmarshalling: " + resource.getURI());
                JaxbUtils.unmarshal(DatacollectionGroup.class, resource);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to unmarshal " + includedGroupFile + ": " + t, t);
            }
        }
    }

    @SuppressWarnings("unused")
	private static <T>T unmarshalJaxb(final Resource resource, final Class<T> clazz) throws JAXBException, IOException {
    	final T obj = JaxbUtils.unmarshal(clazz, new InputSource(resource.getInputStream()));
        assertNotNull("unmarshalled object should not be null after unmarshalling from " + resource, obj);
        m_filesTested.add(resource.getURI().toString());
        return obj;
    }

    private static <T>T unmarshalJaxb(final String configFile, final Class<T> clazz) throws JAXBException, IOException {
        return unmarshalJaxb(ConfigurationTestUtils.getFileForConfigFile(configFile), clazz, m_filesTested, configFile);
    }

    private static <T>T unmarshalJaxbExample(final String configFile, final Class<T> clazz) throws JAXBException, IOException {
        return unmarshalJaxb(ConfigurationTestUtils.getFileForConfigFile("examples/" + configFile), clazz, m_exampleFilesTested, configFile);
    }

    private static <T> T unmarshalJaxb(final File file, final Class<T> clazz, final Set<String> testedSet, final String fileName) throws JAXBException, IOException {
    	final T obj = JaxbUtils.unmarshal(clazz, file);
        assertNotNull("unmarshalled object should not be null after unmarshalling from " + file.getAbsolutePath(), obj);
        testedSet.add(fileName);
        return obj;
    }

    private static <T>T unmarshal(String configFile, Class<T> clazz) throws MarshalException, ValidationException, IOException {
        return unmarshal(ConfigurationTestUtils.getFileForConfigFile(configFile), clazz, m_filesTested, configFile);
    }

    private static <T>T unmarshalExample(String configFile, Class<T> clazz) throws MarshalException, ValidationException, IOException {
        return unmarshal(ConfigurationTestUtils.getFileForConfigFile("examples/" + configFile), clazz, m_exampleFilesTested, configFile);
    }

    private static <T>T unmarshal(File file, Class<T> clazz, Set<String> testedSet, String fileName) throws MarshalException, ValidationException, IOException {
        // Be conservative about what we ship, so don't be lenient
        LocalConfiguration.getInstance().getProperties().remove(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY);
        
        Resource resource = new FileSystemResource(file);
        System.out.println("Unmarshalling: " + resource.getURI());
        T config = CastorUtils.unmarshal(clazz, resource);
        
        assertNotNull("unmarshalled object should not be null after unmarshalling from " + file.getAbsolutePath(), config);
        testedSet.add(fileName);
        
        return config;
    }

    private void unmarshalAndAnticipateException(final String file,  final String exceptionText) throws ValidationException, IOException, AssertionFailedError {
        boolean gotException = false;
        try {
            JaxbUtils.unmarshal(Events.class, ConfigurationTestUtils.getSpringResourceForResource(this, file));
        } catch (final Exception e) {
            if (e.getMessage().contains(exceptionText)) {
                gotException = true;
            } else {
                AssertionFailedError newE = new AssertionFailedError("unmarshal threw an exception but did not contain expected text: " + exceptionText);
                newE.initCause(e);
                throw newE;
            }
        }

        if (!gotException) {
            fail("unmarshal did not throw exception containing expected text: " + exceptionText);
        }
    }
}

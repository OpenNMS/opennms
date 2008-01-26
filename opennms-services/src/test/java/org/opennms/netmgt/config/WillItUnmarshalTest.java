/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jan 25: Add Hyperic tests and test for subdirectories in examples. - dj@opennms.org
 * 2007 Aug 03: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 Daniel J. Gregor, Jr.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.actiond.ActiondConfiguration;
import org.opennms.netmgt.config.archiver.events.EventsArchiverConfiguration;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.JmxDatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.destinationPaths.DestinationPaths;
import org.opennms.netmgt.config.dhcpd.DhcpdConfiguration;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.filter.DatabaseSchema;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.mailtransporttest.MailTransportTest;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.nsclient.NsclientConfig;
import org.opennms.netmgt.config.nsclient.NsclientDatacollectionConfig;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.rtc.RTCConfiguration;
import org.opennms.netmgt.config.scriptd.ScriptdConfiguration;
import org.opennms.netmgt.config.server.LocalServer;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.syslogd.SyslogdConfiguration;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.config.translator.EventTranslatorConfiguration;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.config.viewsdisplay.Viewinfo;
import org.opennms.netmgt.config.vulnscand.VulnscandConfiguration;
import org.opennms.netmgt.config.webuiColors.CategoryColors;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcdConfiguration;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.util.StringUtils;

/**
 * The name of this class is a tribute to
 * <a href="http://www.willitblend.com/">www.willitblend.com</a>.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class WillItUnmarshalTest extends TestCase {
    private static final String CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY = "org.exolab.castor.xml.lenient.sequence.order";
    private static Set<String> m_filesTested = new HashSet<String>();
    private static Set<String> m_exampleFilesTested = new HashSet<String>();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging(true, "INFO");
        
        // Reload castor properties every time since some tests fiddle with them
        LocalConfiguration.getInstance().getProperties().clear();
        LocalConfiguration.getInstance().getProperties().load(ConfigurationTestUtils.getInputStreamForResource(this, "/castor.properties"));
    }
    
    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Ensure we can load a good configuration file without enabling
     * lenient sequence ordering.
     */
    public void testGoodOrdering() throws Exception {
        LocalConfiguration.getInstance().getProperties().remove(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY);

        CastorUtils.unmarshal(Events.class, ConfigurationTestUtils.getReaderForResource(this, "eventconf-good-ordering.xml"));
    }

    /**
     * Ensure we can load a bad configuration file with
     * lenient sequence ordering enabled explicitly.
     */
    public void testLenientOrdering() throws Exception {
        LocalConfiguration.getInstance().getProperties().put(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY, "true");

        CastorUtils.unmarshal(Events.class, ConfigurationTestUtils.getReaderForResource(this, "eventconf-bad-ordering.xml"));
    }

    /**
     * Ensure we can load a bad configuration file with
     * lenient sequence ordering enabled in castor.properties.
     */
    public void testLenientOrderingAsDefault() throws Exception {
        CastorUtils.unmarshal(Events.class, ConfigurationTestUtils.getReaderForResource(this, "eventconf-bad-ordering.xml"));
    }
    
    /**
     * Ensure we fail to load a bad configuration file with
     * lenient sequence ordering disabled explicitly.
     */
    public void testLenientOrderingDisabled() throws Exception {
        LocalConfiguration.getInstance().getProperties().remove(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY);

        String exceptionText = "Element with name event passed to type events in incorrect order";
        boolean gotException = false;

        try {
            CastorUtils.unmarshal(Events.class, ConfigurationTestUtils.getReaderForResource(this, "eventconf-bad-ordering.xml"));
        } catch (MarshalException e) {
            if (e.getMessage().contains(exceptionText)) {
                gotException = true;
            } else {
                AssertionFailedError newE = new AssertionFailedError("unmarshal threw MarshalException but did not contain expected text: " + exceptionText);
                newE.initCause(e);
                throw newE;
            }
        }

        if (!gotException) {
            fail("unmarshal did not throw MarshalException containing expected text: " + exceptionText);
        }
    }

    public void testActiondConfiguration() throws Exception {
        unmarshal("actiond-configuration.xml", ActiondConfiguration.class);
    }
    public void testCapsdConfiguration() throws Exception {
        unmarshal("capsd-configuration.xml", CapsdConfiguration.class);
    }
    public void testExampleCapsdConfiguration() throws Exception {
        unmarshalExample("capsd-configuration.xml", CapsdConfiguration.class);
    }
    public void testExampleHypericCapsdConfiguration() throws Exception {
        unmarshalExample("hyperic-integration/capsd-configuration.xml", CapsdConfiguration.class);
    }
    public void testCategories() throws Exception {
        unmarshal("categories.xml", Catinfo.class);
    }
    public void testChartConfiguration() throws Exception {
        unmarshal("chart-configuration.xml", ChartConfiguration.class);
    }
    public void testCollectdConfiguration() throws Exception {
        unmarshal("collectd-configuration.xml", CollectdConfiguration.class);
    }
    public void testExampleCollectdConfiguration() throws Exception {
        unmarshalExample("collectd-configuration.xml", CollectdConfiguration.class);
    }
    public void testDatabaseSchema() throws Exception {
        unmarshal("database-schema.xml", DatabaseSchema.class);
    }
    public void testDataCollectionConfiguration() throws Exception {
        unmarshal("datacollection-config.xml", DatacollectionConfig.class);
    }
    public void testDestinationPaths() throws Exception {
        unmarshal("destinationPaths.xml", DestinationPaths.class);
    }
    public void testExampleDestinationPaths() throws Exception {
        unmarshalExample("destinationPaths.xml", DestinationPaths.class);
    }
    public void testDhcpdConfiguration() throws Exception {
        unmarshal("dhcpd-configuration.xml", DhcpdConfiguration.class);
    }
    public void testDiscoveryConfiguration() throws Exception {
        unmarshal("discovery-configuration.xml", DiscoveryConfiguration.class);
    }
    public void testEventconf() throws Exception {
        unmarshal("eventconf.xml", Events.class);
    }
    public void testExampleHypericEventconf() throws Exception {
        unmarshalExample("hyperic-integration/eventconf.xml", Events.class);
    }
    public void testExampleHypericEvents() throws Exception {
        unmarshalExample("hyperic-integration/Hyperic.events.xml", Events.class);
    }
    public void testEventsArchiverConfiguration() throws Exception {
        unmarshal("events-archiver-configuration.xml", EventsArchiverConfiguration.class);
    }
    public void testGroups() throws Exception {
        unmarshal("groups.xml", Groupinfo.class);
    }
    public void testExampleGroups() throws Exception {
        unmarshalExample("groups.xml", Groupinfo.class);
    }
    public void testHttpDataCollectionConfiguration() throws Exception {
        unmarshal("http-datacollection-config.xml", HttpDatacollectionConfig.class);
    }
    public void testJmxDataCollectionConfiguration() throws Exception {
        unmarshal("jmx-datacollection-config.xml", JmxDatacollectionConfig.class);
    }
    public void testKscPerformanceReports() throws Exception {
        unmarshal("ksc-performance-reports.xml", ReportsList.class);
    }
    public void testLinkdConfiguration() throws Exception {
        unmarshal("linkd-configuration.xml", LinkdConfiguration.class);
    }
    public void testExampleMailTransportTest() throws Exception {
        unmarshalExample("mail-transport-test.xml", MailTransportTest.class);
    }
    public void testExampleHypericImportsHQ() throws Exception {
        unmarshalExample("hyperic-integration/imports-HQ.xml", ModelImport.class);
    }
    public void testExampleHypericImportsOpennmsAdmin() throws Exception {
        unmarshalExample("hyperic-integration/imports-opennms-admin.xml", ModelImport.class);
    }
    public void testMonitoringLocations() throws Exception {
        unmarshal("monitoring-locations.xml", MonitoringLocationsConfiguration.class);
    }
    public void testExampleMonitoringLocations() throws Exception {
        unmarshalExample("monitoring-locations.xml", MonitoringLocationsConfiguration.class);
    }
    public void testNotifdConfiguration() throws Exception {
        unmarshal("notifd-configuration.xml", NotifdConfiguration.class);
    }
    public void testNotificationCommands() throws Exception {
        unmarshal("notificationCommands.xml", NotificationCommands.class);
    }
    public void testNotifications() throws Exception {
        unmarshal("notifications.xml", Notifications.class);
    }
    public void testExampleNotifications() throws Exception {
        unmarshalExample("notifications.xml", Notifications.class);
    }
    public void testNsclientConfiguration() throws Exception {
        unmarshal("nsclient-config.xml", NsclientConfig.class);
    }
    public void testExampleNsclientConfiguration() throws Exception {
        unmarshalExample("nsclient-config.xml", NsclientConfig.class);
    }
    public void testNsclientDataCollectionConfiguration() throws Exception {
        unmarshal("nsclient-datacollection-config.xml", NsclientDatacollectionConfig.class);
    }
    public void testOpennmsDatasources() throws Exception {
        unmarshal("opennms-datasources.xml", DataSourceConfiguration.class);
    }
    public void testOpennmsServer() throws Exception {
        unmarshal("opennms-server.xml", LocalServer.class);
    }
    public void testExampleOpennmsServer() throws Exception {
        unmarshalExample("opennms-server.xml", LocalServer.class);
    }
    public void testPollOutages() throws Exception {
        unmarshal("poll-outages.xml", Outages.class);
    }
    public void testExamplePollOutages() throws Exception {
        unmarshalExample("poll-outages.xml", Outages.class);
    }
    public void testPollerConfiguration() throws Exception {
        unmarshal("poller-configuration.xml", PollerConfiguration.class);
    }
    public void testExamplePollerConfiguration() throws Exception {
        unmarshalExample("poller-configuration.xml", PollerConfiguration.class);
    }
    public void testExampleHypericPollerConfiguration() throws Exception {
        unmarshalExample("hyperic-integration/poller-configuration.xml", PollerConfiguration.class);
    }
    public void testRtcConfiguration() throws Exception {
        unmarshal("rtc-configuration.xml", RTCConfiguration.class);
    }
    public void testScriptdConfiguration() throws Exception {
        unmarshal("scriptd-configuration.xml", ScriptdConfiguration.class);
    }
    public void testExampleScriptdConfiguration() throws Exception {
        unmarshalExample("scriptd-configuration.xml", ScriptdConfiguration.class);
    }
    public void testSiteStatusViews() throws Exception {
        unmarshal("site-status-views.xml", SiteStatusViewConfiguration.class);
    }
    public void testSnmpConfig() throws Exception {
        unmarshal("snmp-config.xml", SnmpConfig.class);
    }
    public void testExampleSnmpConfig() throws Exception {
        unmarshalExample("snmp-config.xml", SnmpConfig.class);
    }
    public void testStatsdConfiguration() throws Exception {
        unmarshal("statsd-configuration.xml", StatisticsDaemonConfiguration.class);
    }
    public void testSurveillanceViews() throws Exception {
        unmarshal("surveillance-views.xml", SurveillanceViewConfiguration.class);
    }
    public void testExampleSurveillanceViews() throws Exception {
        unmarshalExample("surveillance-views.xml", SurveillanceViewConfiguration.class);
    }
    public void testSyslogdConfiguration() throws Exception {
        unmarshal("syslogd-configuration.xml", SyslogdConfiguration.class);
    }
    public void testThreshdConfiguration() throws Exception {
        unmarshal("threshd-configuration.xml", ThreshdConfiguration.class);
    }
    public void testExampleThreshdConfiguration() throws Exception {
        unmarshalExample("threshd-configuration.xml", ThreshdConfiguration.class);
    }
    public void testThresholds() throws Exception {
        unmarshal("thresholds.xml", ThresholdingConfig.class);
    }
    public void testExampleThresholds() throws Exception {
        unmarshalExample("thresholds.xml", ThresholdingConfig.class);
    }
    public void testTranslatorConfiguration() throws Exception {
        unmarshal("translator-configuration.xml", EventTranslatorConfiguration.class);
    }
    public void testExampleHypericTranslatorConfiguration() throws Exception {
        unmarshalExample("hyperic-integration/translator-configuration.xml", EventTranslatorConfiguration.class);
    }
    public void testTrapdonfiguration() throws Exception {
        unmarshal("trapd-configuration.xml", TrapdConfiguration.class);
    }
    public void testUsers() throws Exception {
        unmarshal("users.xml", Userinfo.class);
    }
    public void testVacuumdConfiguration() throws Exception {
        unmarshal("vacuumd-configuration.xml", VacuumdConfiguration.class);
    }
    public void testVulnscandConfiguration() throws Exception {
        unmarshal("vulnscand-configuration.xml", VulnscandConfiguration.class);
    }
    public void testWebUiColors() throws Exception {
        unmarshal("webui-colors.xml", CategoryColors.class);
    }
    public void testXmlrpcdConfiguration() throws Exception {
        unmarshal("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class);
    }
    public void testEventdConfiguration() throws Exception {
        unmarshal("eventd-configuration.xml", Viewinfo.class);
    }
    public void testServiceConfiguration() throws Exception {
        unmarshal("service-configuration.xml", ServiceConfiguration.class);
    }
    public void testViewsDisplay() throws Exception {
        unmarshal("viewsdisplay.xml", Viewinfo.class);
    }
    public void testExampleViewsDisplay() throws Exception {
        unmarshalExample("viewsdisplay.xml", Viewinfo.class);
    }
    
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
    
    public void testCheckAllDaemonXmlExampleConfigFilesTested() {
        File someConfigFile = ConfigurationTestUtils.getFileForConfigFile("discovery-configuration.xml");
        File examplesDir = new File(someConfigFile.getParentFile(), "examples");

        Set<String> allXml = new HashSet<String>();
        findConfigurationFilesInDirectory(examplesDir, null, allXml);
        
        allXml.removeAll(m_exampleFilesTested);
        
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
                CastorUtils.unmarshal(Events.class, new FileReader(includedEventFile));
            } catch (Throwable t) {
                throw new RuntimeException("Failed to unmarshal " + includedEventFile + ": " + t, t);
            }
        }
    }
    
    private static <T>T unmarshal(String configFile, Class<T> clazz) throws MarshalException, ValidationException, FileNotFoundException {
        return unmarshal(ConfigurationTestUtils.getFileForConfigFile(configFile), clazz, m_filesTested, configFile);
    }

    private static <T>T unmarshalExample(String configFile, Class<T> clazz) throws MarshalException, ValidationException, FileNotFoundException {
        return unmarshal(ConfigurationTestUtils.getFileForConfigFile("examples/" + configFile), clazz, m_exampleFilesTested, configFile);
    }

    private static <T>T unmarshal(File file, Class<T> clazz, Set<String> testedSet, String fileName) throws MarshalException, ValidationException, FileNotFoundException {
        // Be conservative about what we ship, so don't be lenient
        LocalConfiguration.getInstance().getProperties().remove(CASTOR_LENIENT_SEQUENCE_ORDERING_PROPERTY);

        T config = CastorUtils.unmarshal(clazz, new FileReader(file));
        
        assertNotNull("unmarshalled object should not be null after unmarshalling from " + file.getAbsolutePath(), config);
        testedSet.add(fileName);
        
        return config;
    }
}

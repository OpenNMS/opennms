/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.test.ConfigurationTestUtils.getDaemonEtcDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.ifttt.config.IfTttConfig;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportsDefinition;
import org.opennms.features.reporting.model.jasperreport.LocalJasperReports;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.netmgt.alarmd.northbounder.bsf.BSFNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.email.EmailNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.snmptrap.SnmpTrapNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.syslog.SyslogNorthbounderConfig;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.actiond.ActiondConfiguration;
import org.opennms.netmgt.config.ami.AmiConfig;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
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
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.config.mailtransporttest.MailTransportTest;
import org.opennms.netmgt.config.microblog.MicroblogConfiguration;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.rancid.adapter.RancidConfiguration;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.config.reporting.OpennmsReports;
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
import org.opennms.netmgt.config.trend.TrendConfiguration;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;
import org.opennms.netmgt.config.viewsdisplay.Viewinfo;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.config.vmware.vijava.VmwareDatacollectionConfig;
import org.opennms.netmgt.config.wmi.WmiDatacollectionConfig;
import org.opennms.netmgt.config.wmi.agent.WmiConfig;
import org.opennms.netmgt.config.wsman.WsmanConfig;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfiguration;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.plugins.elasticsearch.rest.credentials.ElasticCredentials;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import junit.framework.AssertionFailedError;

/**
 * This is an integration test checking if all provided example XML files can be
 * unmarshalled.
 * 
 * For each file to test, an entry in the {@link #files()} list must exist.
 * During test run, all tests methods are executed for each test.
 * 
 * To ensure, that all provided files are covered, a meta test is used
 * ({@link WillItUnmarshalMetaTest}).
 * 
 * The name of this class is a tribute to
 * <a href="http://www.willitblend.com/">www.willitblend.com</a>.
 * 
 * @author Dustin Frisch<fooker@lab.sh>
 * 
 * @see WillItUnmarshalMetaTest
 */
@RunWith(value = Parameterized.class)
public class WillItUnmarshalIT {

    private static final Pattern COMMENT_START_PATTERN = Pattern.compile("\\s*[\\r\\n]*\\s*<!--", Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Possible implementations for resource loading.
     */
    public static enum Source {
        CONFIG,
        EXAMPLE,
        SPRING,
        ABSOLUTE,
    }

    /**
     * A list of test parameters to execute.
     * 
     * See {@link #files()} for detailed information.
     */
    public static final ArrayList<Object[]> FILES = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        XMLUnit.setIgnoreWhitespace(false);
        XMLUnit.setIgnoreAttributeOrder(false);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        XMLUnit.setNormalize(false);
    }

    /**
     * Add a file to the list of XML files to test.
     * @param source The {@link Source} type
     * @param file the file to unmarshal
     * @param clazz the class it unmarshals to
     * @param checkFormat
     * @param exceptionMessage
     */
    private static void addFile(final Source source, final String file, final Class<?> clazz, boolean checkFormat, final String exceptionMessage) {
        FILES.add(new Object[] {source, file, clazz, checkFormat, exceptionMessage});
    }

    static {
        addFile(Source.SPRING, "eventconf-good-ordering.xml", Events.class, false, null);
        addFile(Source.SPRING, "eventconf-bad-ordering.xml", Events.class, false, null);

        addFile(Source.SPRING, "eventconf-bad-element.xml", Events.class, false, "Invalid content was found starting with element 'bad-element'.");

        addFile(Source.CONFIG, "ackd-configuration.xml", AckdConfiguration.class, false, null);
        addFile(Source.CONFIG, "actiond-configuration.xml", ActiondConfiguration.class, true, null);
        addFile(Source.CONFIG, "ami-config.xml", AmiConfig.class, true, null);
        addFile(Source.CONFIG, "availability-reports.xml", OpennmsReports.class, false, null);
        addFile(Source.CONFIG, "bsf-northbounder-configuration.xml", BSFNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "categories.xml", Catinfo.class, false, null);
        addFile(Source.CONFIG, "chart-configuration.xml", ChartConfiguration.class, true, null);
        addFile(Source.CONFIG, "collectd-configuration.xml", CollectdConfiguration.class, true, null);
        addFile(Source.CONFIG, "database-reports.xml", LegacyLocalReportsDefinition.class, false, null);
        addFile(Source.CONFIG, "database-schema.xml", DatabaseSchema.class, true, null);
        addFile(Source.CONFIG, "datacollection-config.xml", DatacollectionConfig.class, true, null);
        addFile(Source.CONFIG, "destinationPaths.xml", DestinationPaths.class, true, null);
        addFile(Source.CONFIG, "discovery-configuration.xml", DiscoveryConfiguration.class, false, null);
        addFile(Source.CONFIG, "drools-northbounder-configuration.xml", DroolsNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "elastic-credentials.xml", ElasticCredentials.class, true, null);
        addFile(Source.CONFIG, "email-northbounder-configuration.xml", EmailNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "enlinkd-configuration.xml", EnlinkdConfiguration.class, false, null);
        addFile(Source.CONFIG, "eventconf.xml", Events.class, true, null);
        addFile(Source.CONFIG, "eventd-configuration.xml", EventdConfiguration.class, true, null);
        addFile(Source.CONFIG, "groups.xml", Groupinfo.class, true, null);
        addFile(Source.CONFIG, "http-datacollection-config.xml", HttpDatacollectionConfig.class, false, null);
        addFile(Source.CONFIG, "ifttt-config.xml", IfTttConfig.class, true, null);
        addFile(Source.CONFIG, "jasper-reports.xml", LocalJasperReports.class, false, null);
        addFile(Source.CONFIG, "javamail-configuration.xml", JavamailConfiguration.class, false, null);
        addFile(Source.CONFIG, "jdbc-datacollection-config.xml", JdbcDataCollectionConfig.class, true, null);
        addFile(Source.CONFIG, "jms-northbounder-configuration.xml", JmsNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "jmx-config.xml", JmxConfig.class, true, null);
        addFile(Source.CONFIG, "jmx-datacollection-config.xml", JmxDatacollectionConfig.class, true, null);
        addFile(Source.CONFIG, "ksc-performance-reports.xml", ReportsList.class, true, null);
        addFile(Source.CONFIG, "microblog-configuration.xml", MicroblogConfiguration.class, false, null);
        addFile(Source.CONFIG, "notifd-configuration.xml", NotifdConfiguration.class, true, null);
        addFile(Source.CONFIG, "notificationCommands.xml", NotificationCommands.class, true, null);
        addFile(Source.CONFIG, "notifications.xml", Notifications.class, true, null);
        addFile(Source.CONFIG, "opennms-datasources.xml", DataSourceConfiguration.class, false, null);
        addFile(Source.CONFIG, "opennms-server.xml", LocalServer.class, true, null);
        addFile(Source.CONFIG, "poll-outages.xml", Outages.class, true, null);
        addFile(Source.CONFIG, "poller-configuration.xml", PollerConfiguration.class, true, null);
        addFile(Source.CONFIG, "provisiond-configuration.xml", ProvisiondConfiguration.class, false, null);
        addFile(Source.CONFIG, "rancid-configuration.xml", RancidConfiguration.class, true, null);
        addFile(Source.CONFIG, "remote-repository.xml", RemoteRepositoryConfig.class, true, null);
        addFile(Source.CONFIG, "reportd-configuration.xml", ReportdConfiguration.class, false, null);
        addFile(Source.CONFIG, "rtc-configuration.xml", RTCConfiguration.class, true, null);
        addFile(Source.CONFIG, "rws-configuration.xml", RwsConfiguration.class, false, null);
        addFile(Source.CONFIG, "scriptd-configuration.xml", ScriptdConfiguration.class, true, null);
        addFile(Source.CONFIG, "service-configuration.xml", ServiceConfiguration.class, false, null);
        addFile(Source.CONFIG, "site-status-views.xml", SiteStatusViewConfiguration.class, true, null);
        addFile(Source.CONFIG, "snmptrap-northbounder-configuration.xml", SnmpTrapNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "snmp-asset-adapter-configuration.xml", SnmpAssetAdapterConfiguration.class, true, null);
        addFile(Source.CONFIG, "snmp-config.xml", SnmpConfig.class, true, null);
        addFile(Source.CONFIG, "snmp-hardware-inventory-adapter-configuration.xml", HwInventoryAdapterConfiguration.class, false, null);
        addFile(Source.CONFIG, "snmp-interface-poller-configuration.xml", SnmpInterfacePollerConfiguration.class, true, null);
        addFile(Source.CONFIG, "statsd-configuration.xml", StatisticsDaemonConfiguration.class, false, null);
        addFile(Source.CONFIG, "surveillance-views.xml", SurveillanceViewConfiguration.class, true, null);
        addFile(Source.CONFIG, "syslog-northbounder-configuration.xml", SyslogNorthbounderConfig.class, true, null);
        addFile(Source.CONFIG, "syslogd-configuration.xml", SyslogdConfiguration.class, false, null);
        addFile(Source.CONFIG, "telemetryd-configuration.xml", TelemetrydConfiguration.class, true, null);
        addFile(Source.CONFIG, "threshd-configuration.xml", ThreshdConfiguration.class, true, null);
        addFile(Source.CONFIG, "thresholds.xml", ThresholdingConfig.class, true, null);
        addFile(Source.CONFIG, "tl1d-configuration.xml", Tl1dConfiguration.class, true, null);
        addFile(Source.CONFIG, "translator-configuration.xml", EventTranslatorConfiguration.class, false, null);
        addFile(Source.CONFIG, "trapd-configuration.xml", TrapdConfiguration.class, true, null);
        addFile(Source.CONFIG, "trend-configuration.xml", TrendConfiguration.class, true, null);
        addFile(Source.CONFIG, "users.xml", Userinfo.class, true, null);
        addFile(Source.CONFIG, "vacuumd-configuration.xml", VacuumdConfiguration.class, false, null);
        addFile(Source.CONFIG, "viewsdisplay.xml", Viewinfo.class, false, null);
        addFile(Source.CONFIG, "vmware-cim-datacollection-config.xml", VmwareCimDatacollectionConfig.class, true, null);
        addFile(Source.CONFIG, "vmware-config.xml", VmwareConfig.class, false, null);
        addFile(Source.CONFIG, "vmware-datacollection-config.xml", VmwareDatacollectionConfig.class, false, null);
        addFile(Source.CONFIG, "wmi-config.xml", WmiConfig.class, true, null);
        addFile(Source.CONFIG, "wmi-datacollection-config.xml", WmiDatacollectionConfig.class, false, null);
        addFile(Source.CONFIG, "wsman-config.xml", WsmanConfig.class, true, null);

        addFile(Source.EXAMPLE, "collectd-configuration.xml", CollectdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "destinationPaths.xml", DestinationPaths.class, false, null);
        addFile(Source.EXAMPLE, "devices/motorola_cpei_150_wimax_gateway/http-datacollection-config.xml", HttpDatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "discovery-configuration.xml", DiscoveryConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "event-proxy/Proxy.events.xml", Events.class, false, null);
        addFile(Source.EXAMPLE, "event-proxy/scriptd-configuration.xml", ScriptdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "event-proxy/vacuumd-configuration.xml", VacuumdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "groups.xml", Groupinfo.class, false, null);
        addFile(Source.EXAMPLE, "hyperic-integration/imports-HQ.xml", Requisition.class, false, null);
        addFile(Source.EXAMPLE, "hyperic-integration/imports-opennms-admin.xml", Requisition.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/collectd-configuration.xml", CollectdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-config.d/activemq.xml", JmxDatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-config.d/cassandra.xml", JmxDatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-config.d/jboss4.xml", JmxDatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-config.d/opennms.xml", JmxDatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/ActiveMQ/5.6/ActiveMQBasic0.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/Cassandra/1.1.2/CassandraBasic0.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/JBoss/4/JBossBasic0.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/Jvm/1.6/JvmBasic0.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/Jvm/1.6/JvmLegacy.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/OpenNMS/1.10/OpenNMSBasic0.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "jvm-datacollection/jmx-datacollection-mbeans/OpenNMS/1.10/OpenNMSLegacy.xml", Mbeans.class, false, null);
        addFile(Source.EXAMPLE, "mail-transport-test.xml", MailTransportTest.class, false, null);
        addFile(Source.EXAMPLE, "notificationCommands.xml", NotificationCommands.class, false, null);
        addFile(Source.EXAMPLE, "notifications.xml", Notifications.class, false, null);
        addFile(Source.EXAMPLE, "old-datacollection-config.xml", DatacollectionConfig.class, false, null);
        addFile(Source.EXAMPLE, "opennms-server.xml", LocalServer.class, false, null);
        addFile(Source.EXAMPLE, "poll-outages.xml", Outages.class, false, null);
        addFile(Source.EXAMPLE, "poller-configuration.xml", PollerConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "rancid-configuration.xml", RancidConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "rws-configuration.xml", RwsConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "scriptd-configuration.xml", ScriptdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "snmp-config.xml", SnmpConfig.class, false, null);
        addFile(Source.EXAMPLE, "surveillance-views.xml", SurveillanceViewConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "threshd-configuration.xml", ThreshdConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "thresholds.xml", ThresholdingConfig.class, false, null);
        addFile(Source.EXAMPLE, "tl1d-configuration.xml", Tl1dConfiguration.class, false, null);
        addFile(Source.EXAMPLE, "viewsdisplay.xml", Viewinfo.class, false, null);

        // Add all event files
        for (final File file : FileUtils.listFiles(new File(getDaemonEtcDirectory(), "events"),
                                                   new String[] { "xml" },
                                                   true)) {
            addFile(Source.ABSOLUTE, file.getPath(), Events.class, false, null);
        }

        // Add all datacollection group files
        for (final File file : FileUtils.listFiles(new File(getDaemonEtcDirectory(), "datacollection"),
                                                   new String[] { "xml" },
                                                   true)) {
            addFile(Source.ABSOLUTE, file.getPath(), DatacollectionGroup.class, false, null);
        }

        // Add all wsman-datacollection configuration files
        addFile(Source.CONFIG, "wsman-datacollection-config.xml", WsmanDatacollectionConfig.class, false, null);
        for (final File file : FileUtils.listFiles(new File(getDaemonEtcDirectory(), "wsman-datacollection.d"),
                                                   new String[] { "xml" },
                                                   true)) {
            addFile(Source.ABSOLUTE, file.getPath(), WsmanDatacollectionConfig.class, false, null);
        }

        // Add all resource-types configuration files
        for (final File file : FileUtils.listFiles(new File(getDaemonEtcDirectory(), "resource-types.d"),
                                                   new String[] { "xml" },
                                                   true)) {
            addFile(Source.ABSOLUTE, file.getPath(), ResourceTypes.class, false, null);
        }

        // Add all jmx-datacollection configuration files
        for (final File file : FileUtils.listFiles(new File(getDaemonEtcDirectory(), "jmx-datacollection-config.d"),
                                                   new String[] { "xml" },
                                                   true)) {
            addFile(Source.ABSOLUTE, file.getPath(), JmxDatacollectionConfig.class, false, null);
        }
    }

    /**
     * The list of files to test.
     * 
     * For each XML file to test, this method must return an entry in the list.
     * Each entry consists of the following parts:
     * <ul>
     *   <li>The source to load the resource from</li>
     *   <li>The file to test</li>
     *   <li>The class used for unmarshaling</li>
     *   <li>Whether to check if the file is in JAXB's default marshal format</li>
     *   <li>An expected exception message</li>
     * </ul>
     * 
     * The returned file list is stored in {@link #FILES} which is filled in the
     * static constructor.
     * 
     * @return list of parameters for the test
     */
    @Parameterized.Parameters
    public static Collection<Object[]> files() {
        return FILES;
    }

    private final Source source;
    private final String file;
    private final Class<?> clazz;
    private final boolean checkFormat;
    private final String exception;

    public WillItUnmarshalIT(final Source source,
            final String file,
            final Class<?> clazz,
            final boolean checkFormat,
            final String exception) {
        this.source = source;
        this.file = file;
        this.clazz = clazz;
        this.checkFormat = checkFormat;
        this.exception = exception;
    }

    @Test
    public void testUnmarshalling() {
        final Resource resource = this.createResource();
        assertNotNull("Resource must not be null", resource);

        // Unmarshall the config file
        Object result = null;
        try {
            result = JaxbUtils.unmarshal(this.clazz, resource);

            // Assert that unmarshalling returned a valid result
            assertNotNull("Unmarshalled instance must not be null", result);

        } catch (final AssertionFailedError ex) {
            throw ex;

        } catch (final Exception ex) {
            // If we have an expected exception, the returned exception muss
            // match - if not the test failed
            if (this.exception != null) {
                assertEquals(this.exception, exception.toString());

            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    @Test
    public void testJaxbFormat() {
        if (!checkFormat) {
            return;
        }
        final Resource resource = this.createResource();
        assertNotNull("Resource must not be null", resource);
        try {
            String onDisk = readResource(resource);
            Object result = JaxbUtils.unmarshal(this.clazz, resource);
            final Pattern p = COMMENT_START_PATTERN;
            final Matcher m = p.matcher(onDisk);
            onDisk = m.replaceAll("<!--");
            final String marshalled = JaxbUtils.marshal(result);
            final List<Difference> diffs = XmlTest.getDifferencesSimple(onDisk, marshalled);
            if (diffs.size() != 0) {
                System.err.println("----------------------------------------------------------------------");
                System.err.println(resource.getFilename() + " on disk:");
                System.err.println(onDisk);
                System.err.println(resource.getFilename() + " marshalled:");
                System.err.println(marshalled);
                throw new AssertionFailedError(resource.getFilename() + ": " + diffs);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String readResource(final Resource resource) {
        try (final InputStream is = resource.getInputStream(); final InputStreamReader isr = new InputStreamReader(is)) {
            return IOUtils.toString(isr);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a resource for the config file to unmarshall using the configured
     * source.
     * 
     * @return the Resource 
     */
    public final Resource createResource() {
        // Create a resource for the config file to unmarshall using the
        // configured source
        switch (this.source) {
        case CONFIG:
            return new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile(file));

        case EXAMPLE:
            return new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("examples/" + file));

        case SPRING:
            return ConfigurationTestUtils.getSpringResourceForResource(this, this.file);

        case ABSOLUTE:
            return new FileSystemResource(this.file);

        default:
            throw new RuntimeException("Source unknown: " + this.source);
        }
    }
}

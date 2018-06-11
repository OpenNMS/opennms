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

package org.opennms.netmgt.config.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.ifttt.config.IfTttConfig;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.netmgt.alarmd.northbounder.bsf.BSFNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.drools.DroolsNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.email.EmailNorthbounderConfig;
import org.opennms.netmgt.alarmd.northbounder.jms.JmsNorthbounderConfig;
import org.opennms.netmgt.config.actiond.ActiondConfiguration;
import org.opennms.netmgt.config.ami.AmiConfig;
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
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.config.kscReports.ReportsList;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notificationCommands.NotificationCommands;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.config.rancid.adapter.RancidConfiguration;
import org.opennms.netmgt.config.rtc.RTCConfiguration;
import org.opennms.netmgt.config.scriptd.ScriptdConfiguration;
import org.opennms.netmgt.config.server.LocalServer;
import org.opennms.netmgt.config.siteStatusViews.SiteStatusViewConfiguration;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmpAsset.adapter.SnmpAssetAdapterConfiguration;
import org.opennms.netmgt.config.snmpinterfacepoller.SnmpInterfacePollerConfiguration;
import org.opennms.netmgt.config.surveillanceViews.SurveillanceViewConfiguration;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.opennms.netmgt.config.tl1d.Tl1dConfiguration;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.config.trend.TrendConfiguration;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.config.wmi.agent.WmiConfig;
import org.opennms.netmgt.config.wsman.WsmanConfig;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Class Normalizer.
 * 
 * This is intended to normalize some configuration files to be consistent with the parser library
 * used to manipulate the files.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Normalizer {

    /**
     * The Class Source.
     */
    public static class Source {

        /** The configuration file. */
        public File configFile;

        /** The object class. */
        public Class<?> clazz;

        /**
         * Instantiates a new source.
         *
         * @param configFile the configuration file
         * @param clazz the object class
         */
        public Source(File configFile, Class<?> clazz) {
            this.configFile = configFile;
            this.clazz = clazz;
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) throws Exception {
        try {
            if (args == null || args.length < 1) {
                usage();
                System.exit(1);
            }

            File sourceFile = new File(args[0]);
            if (!sourceFile.exists()) {
                System.err.println("Configuration source '" + sourceFile + "' doesn't exist.");
                System.exit(1);
            }

            if (sourceFile.isFile()) {
                // source File is an individual file
                final String className = args[1];
                if (className == null) {
                    usage();
                    System.exit(1);
                }
                final Class<?> c = Class.forName(className);
                final Source source = new Source(sourceFile, c);
                Normalizer.unmarshal(source);
            } else {
                // source File is a config directory
                new Normalizer(sourceFile).normalize();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.err.print(
            "usage: normalizer <directory|file> [class]\n"
            + "\n"
            + "Normalizing the OpenNMS etc directory:\n"
            + "$ normalizer /path/to/etc/directory\n"
            + "eg: $ normalizer /home/you/git/opennms/opennms-base-assembly/src/main/filtered/etc\n"
            + "\n"
            + "Normalizing an individual file:\n"
            + "$ normalizer /path/to/config/file.xml org.opennms.the.ClassName\n"
            + "eg: $ normalizer poller-configuration.xml org.opennms.netmgt.config.poller.PollerConfiguration\n"
            + "\n"
        );
    }

    /** The configuration home. */
    private File configHome;

    /** The configuration sources. */
    private ArrayList<Source> configSources = new ArrayList<Source>();

    /**
     * Adds the file.
     *
     * @param fileName the file name
     * @param clazz the object class
     */
    private void addFile(final String fileName, final Class<?> clazz) {
        File file = new File(configHome, fileName);
        if (file.exists()) {
            configSources.add(new Source(file, clazz));
        } else {
            System.out.println("Not Found : " + file);
        }
    }

    /**
     * Instantiates a new normalizer.
     *
     * @param configHome the configuration home (for example: /opt/opennms/etc, or /etc/opennms)
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Normalizer(File configHome) throws IOException {
        this.configHome = configHome;
        System.out.println("Normalizing OpenNMS configuration files at " + configHome);

        // This will re-format only the required files
        // Keep in mind that all comments are going to be removed from the files

//      addFile("ackd-configuration.xml", AckdConfiguration.class); // It has manual changes (with comments)
        addFile("actiond-configuration.xml", ActiondConfiguration.class);
        addFile("ami-config.xml", AmiConfig.class);
//      addFile("availability-reports.xml", OpennmsReports.class); // It has manual changes
        addFile("bsf-northbounder-configuration.xml", BSFNorthbounderConfig.class);
//      addFile("capsd-configuration.xml", CapsdConfiguration.class);
//      addFile("categories.xml", Catinfo.class); // Current format is better. Some manual adjustments have been added.
        addFile("chart-configuration.xml", ChartConfiguration.class);
        addFile("collectd-configuration.xml", CollectdConfiguration.class);
//      addFile("database-reports.xml", LegacyLocalReportsDefinition.class); // Current format is better. Some manual adjustments have been added.
        addFile("database-schema.xml", DatabaseSchema.class);
        addFile("datacollection-config.xml", DatacollectionConfig.class);
        addFile("destinationPaths.xml", DestinationPaths.class);
        addFile("discovery-configuration.xml", DiscoveryConfiguration.class);
        addFile("drools-northbounder-configuration.xml", DroolsNorthbounderConfig.class);
//      addFile("enlinkd-configuration.xml", EnlinkdConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("email-northbounder-configuration.xml", EmailNorthbounderConfig.class);
        addFile("eventconf.xml", Events.class);
        addFile("eventd-configuration.xml", EventdConfiguration.class);
        addFile("groups.xml", Groupinfo.class);
//      addFile("http-datacollection-config.xml", HttpDatacollectionConfig.class); // Current format is better. Some manual adjustments have been added.
        addFile("ifttt-config.xml", IfTttConfig.class);
//      addFile("jasper-reports.xml", LocalJasperReports.class); // Some manual adjustments have been added after normalization
//      addFile("javamail-configuration.xml", JavamailConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("jdbc-datacollection-config.xml", JdbcDataCollectionConfig.class); // TODO It has manual changes
        addFile("jms-northbounder-configuration.xml", JmsNorthbounderConfig.class);
        addFile("jmx-config.xml", JmxConfig.class);
        addFile("jmx-datacollection-config.xml", JmxDatacollectionConfig.class); // FIXME Move commented data to examples
        addFile("ksc-performance-reports.xml", ReportsList.class);
//      addFile("mapsadapter-configuration.xml", MapsAdapterConfiguration.class);
//      addFile("microblog-configuration.xml", MicroblogConfiguration.class); // Current format is better. Some manual adjustments have been added.
//      addFile("monitoring-locations.xml", MonitoringLocationsConfiguration.class);
        addFile("notifd-configuration.xml", NotifdConfiguration.class);
        addFile("notificationCommands.xml", NotificationCommands.class);
        addFile("notifications.xml", Notifications.class);
//      addFile("opennms-datasources.xml", DataSourceConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("opennms-server.xml", LocalServer.class);
        addFile("poll-outages.xml", Outages.class);
        addFile("poller-configuration.xml", PollerConfiguration.class); // TODO It has manual changes because of comments
//      addFile("provisiond-configuration.xml", ProvisiondConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("rancid-configuration.xml", RancidConfiguration.class);
        addFile("remote-repository.xml", RemoteRepositoryConfig.class);
//      addFile("reportd-configuration.xml", ReportdConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("rtc-configuration.xml", RTCConfiguration.class);
//      addFile("rws-configuration.xml", RwsConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("scriptd-configuration.xml", ScriptdConfiguration.class);
//      addFile("service-configuration.xml", ServiceConfiguration.class);  // TODO It has manual changes because of comments
        addFile("site-status-views.xml", SiteStatusViewConfiguration.class);
        addFile("snmp-asset-adapter-configuration.xml", SnmpAssetAdapterConfiguration.class);
        addFile("snmp-config.xml", SnmpConfig.class);
//      addFile("snmp-hardware-inventory-adapter-configuration.xml", HwInventoryAdapterConfiguration.class);
        addFile("snmp-interface-poller-configuration.xml", SnmpInterfacePollerConfiguration.class);
//      addFile("snmptrap-northbounder-configuration.xml", SnmpTrapNorthbounderConfig.class); // Current format is better. Some manual adjustments have been added.
//      addFile("statsd-configuration.xml", StatisticsDaemonConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("surveillance-views.xml", SurveillanceViewConfiguration.class);
//      addFile("syslog-northbounder-configuration.xml", SyslogNorthbounderConfig.class); // Current format is better. Some manual adjustments have been added.
//      addFile("syslogd-configuration.xml", SyslogdConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("threshd-configuration.xml", ThreshdConfiguration.class);
        addFile("thresholds.xml", ThresholdingConfig.class);
        addFile("tl1d-configuration.xml", Tl1dConfiguration.class);
//      addFile("translator-configuration.xml", EventTranslatorConfiguration.class); // Current format is better. Some manual adjustments have been added.
        addFile("trapd-configuration.xml", TrapdConfiguration.class);
        addFile("trend-configuration.xml", TrendConfiguration.class);
        addFile("users.xml", Userinfo.class);
//      addFile("vacuumd-configuration.xml", VacuumdConfiguration.class); // Current format is better. Some manual adjustments have been added.
//      addFile("viewsdisplay.xml", Viewinfo.class); // Current format is better. Some manual adjustments have been added.
        addFile("vmware-cim-datacollection-config.xml", VmwareCimDatacollectionConfig.class);
//      addFile("vmware-config.xml", VmwareConfig.class); // Current format is better. Some manual adjustments have been added.
//      addFile("vmware-datacollection-config.xml", VmwareDatacollectionConfig.class); // Added comments
        addFile("wsman-config.xml", WsmanConfig.class);
        addFile("wmi-config.xml", WmiConfig.class);
//      addFile("wmi-datacollection-config.xml", WmiDatacollectionConfig.class); // Current format is better. Some manual adjustments have been added.
//      addFile("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class);

        for (final File file : FileUtils.listFiles(new File(configHome, "events"), new String[] { "xml" }, true)) {
            addFile("events/" + file.getName(), Events.class);
        }
        for (final File file : FileUtils.listFiles(new File(configHome, "datacollection"), new String[] { "xml" }, true)) {
            addFile("datacollection/" + file.getName(), DatacollectionGroup.class);
        }
    }

    /**
     * Normalize.
     *
     * @throws Exception the exception
     */
    public void normalize() throws Exception {
        for (final Source source : configSources) {
            Normalizer.unmarshal(source);
        }
    }

    /**
     * Unmarshall.
     *
     * @param source the source
     */
    public static void unmarshal(Source source) {
        try {
            System.out.println("Normalizing " + source.configFile);
            final Resource resource = new FileSystemResource(source.configFile);
            Object result = null;
            result = JaxbUtils.unmarshal(source.clazz, resource);
            JaxbUtils.marshal(result, new FileWriter(source.configFile));
            result = JaxbUtils.unmarshal(source.clazz, resource); // Double check the newly generated file can be processed correctly.
            if (result == null) {
                throw new IllegalArgumentException("Something went wrong in JAXB.");
            }
        } catch(Exception ex) {
            System.err.println("Cannot normalize file: " + ex);
        }
    }
}

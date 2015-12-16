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
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
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
     * The Enumeration Implementation.
     */
    public static enum Implementation {

        /** The jaxb. */
        JAXB,

        /** The castor. */
        CASTOR
    }

    /**
     * The Class Source.
     */
    public static class Source {

        /** The configuration file. */
        public File configFile;

        /** The object class. */
        public Class<?> clazz;

        /** The implementation. */
        public Implementation implementation;

        /**
         * Instantiates a new source.
         *
         * @param configFile the configuration file
         * @param clazz the object class
         * @param implementation the implementation
         */
        public Source(File configFile, Class<?> clazz, Implementation implementation) {
            this.configFile = configFile;
            this.clazz = clazz;
            this.implementation = implementation;
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length < 1) {
                System.err.println("Please specify the configuration directory to normalize");
                System.exit(1);
            }
            File configHome = new File(args[0]);
            if (!configHome.exists()) {
                System.err.println("The configuration directory " + configHome + " doesn't exist");
                System.exit(1);
            }
            new Normalizer(configHome).normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @param implementation the implementation
     */
    private void addFile(final String fileName, final Class<?> clazz, final Implementation implementation) {
        File file = new File(configHome, fileName);
        if (file.exists()) {
            configSources.add(new Source(file, clazz, implementation));
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

        LocalConfiguration.getInstance().getProperties().clear();
        LocalConfiguration.getInstance().getProperties().load(getClass().getResourceAsStream("/castor.properties"));

        // This will re-format only the required files
        // Keep in mind that all comments are going to be removed from the files

//      addFile("ackd-configuration.xml", AckdConfiguration.class, Implementation.JAXB); // It has manual changes (with comments)
        addFile("actiond-configuration.xml", ActiondConfiguration.class, Implementation.JAXB);
        addFile("ami-config.xml", AmiConfig.class, Implementation.JAXB);
//      addFile("availability-reports.xml", OpennmsReports.class, Implementation.CASTOR); // It has manual changes
//      addFile("capsd-configuration.xml", CapsdConfiguration.class, Implementation.CASTOR);
//      addFile("categories.xml", Catinfo.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("chart-configuration.xml", ChartConfiguration.class, Implementation.CASTOR);
        addFile("collectd-configuration.xml", CollectdConfiguration.class, Implementation.JAXB);
//      addFile("database-reports.xml", LegacyLocalReportsDefinition.class, Implementation.JAXB); // Current format is better. Some manual adjustments have been added.
        addFile("database-schema.xml", DatabaseSchema.class, Implementation.CASTOR);
        addFile("datacollection-config.xml", DatacollectionConfig.class, Implementation.JAXB);
        addFile("destinationPaths.xml", DestinationPaths.class, Implementation.CASTOR);
        addFile("discovery-configuration.xml", DiscoveryConfiguration.class, Implementation.CASTOR);
//      addFile("enlinkd-configuration.xml", EnlinkdConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("eventconf.xml", Events.class, Implementation.JAXB);
        addFile("eventd-configuration.xml", EventdConfiguration.class, Implementation.CASTOR);
        addFile("groups.xml", Groupinfo.class, Implementation.CASTOR);
//      addFile("http-datacollection-config.xml", HttpDatacollectionConfig.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
//      addFile("jasper-reports.xml", LocalJasperReports.class, Implementation.JAXB); // Some manual adjustments have been added after normalization
//      addFile("javamail-configuration.xml", JavamailConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("jdbc-datacollection-config.xml", JdbcDataCollectionConfig.class, Implementation.JAXB); // TODO It has manual changes
        addFile("jmx-datacollection-config.xml", JmxDatacollectionConfig.class, Implementation.JAXB); // FIXME Move commented data to examples
        addFile("ksc-performance-reports.xml", ReportsList.class, Implementation.CASTOR);
//      addFile("linkd-configuration.xml", LinkdConfiguration.class, Implementation.CASTOR);
//      addFile("mapsadapter-configuration.xml", MapsAdapterConfiguration.class, Implementation.CASTOR);
//      addFile("microblog-configuration.xml", MicroblogConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
//      addFile("monitoring-locations.xml", MonitoringLocationsConfiguration.class, Implementation.JAXB);
        addFile("notifd-configuration.xml", NotifdConfiguration.class, Implementation.CASTOR);
        addFile("notificationCommands.xml", NotificationCommands.class, Implementation.CASTOR);
        addFile("notifications.xml", Notifications.class, Implementation.CASTOR);
//      addFile("opennms-datasources.xml", DataSourceConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("opennms-server.xml", LocalServer.class, Implementation.JAXB);
        addFile("poll-outages.xml", Outages.class, Implementation.JAXB);
        addFile("poller-configuration.xml", PollerConfiguration.class, Implementation.JAXB); // TODO It has manual changes because of comments
//      addFile("provisiond-configuration.xml", ProvisiondConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("rancid-configuration.xml", RancidConfiguration.class, Implementation.CASTOR);
        addFile("remote-repository.xml", RemoteRepositoryConfig.class, Implementation.JAXB);
//      addFile("reportd-configuration.xml", ReportdConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("rtc-configuration.xml", RTCConfiguration.class, Implementation.CASTOR);
//      addFile("rws-configuration.xml", RwsConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("scriptd-configuration.xml", ScriptdConfiguration.class, Implementation.CASTOR);
//      addFile("service-configuration.xml", ServiceConfiguration.class, Implementation.JAXB);  // TODO It has manual changes because of comments
        addFile("site-status-views.xml", SiteStatusViewConfiguration.class, Implementation.CASTOR);
        addFile("snmp-asset-adapter-configuration.xml", SnmpAssetAdapterConfiguration.class, Implementation.CASTOR);
        addFile("snmp-config.xml", SnmpConfig.class, Implementation.JAXB);
//      addFile("snmp-hardware-inventory-adapter-configuration.xml", HwInventoryAdapterConfiguration.class, Implementation.JAXB);
        addFile("snmp-interface-poller-configuration.xml", SnmpInterfacePollerConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
//      addFile("statsd-configuration.xml", StatisticsDaemonConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("surveillance-views.xml", SurveillanceViewConfiguration.class, Implementation.CASTOR);
//      addFile("syslog-northbounder-configuration.xml", SyslogNorthbounderConfig.class, Implementation.JAXB); // Current format is better. Some manual adjustments have been added.
//      addFile("syslogd-configuration.xml", SyslogdConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("threshd-configuration.xml", ThreshdConfiguration.class, Implementation.CASTOR);
        addFile("thresholds.xml", ThresholdingConfig.class, Implementation.CASTOR);
        addFile("tl1d-configuration.xml", Tl1dConfiguration.class, Implementation.CASTOR);
//      addFile("translator-configuration.xml", EventTranslatorConfiguration.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("trapd-configuration.xml", TrapdConfiguration.class, Implementation.CASTOR);
        addFile("users.xml", Userinfo.class, Implementation.CASTOR);
//      addFile("vacuumd-configuration.xml", VacuumdConfiguration.class, Implementation.JAXB); // Current format is better. Some manual adjustments have been added.
//      addFile("viewsdisplay.xml", Viewinfo.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
        addFile("vmware-cim-datacollection-config.xml", VmwareCimDatacollectionConfig.class, Implementation.JAXB);
//      addFile("vmware-config.xml", VmwareConfig.class, Implementation.JAXB); // Current format is better. Some manual adjustments have been added.
//      addFile("vmware-datacollection-config.xml", VmwareDatacollectionConfig.class, Implementation.JAXB); // Added comments
        addFile("wmi-config.xml", WmiConfig.class, Implementation.CASTOR);
//      addFile("wmi-datacollection-config.xml", WmiDatacollectionConfig.class, Implementation.CASTOR); // Current format is better. Some manual adjustments have been added.
//      addFile("xmlrpcd-configuration.xml", XmlrpcdConfiguration.class, Implementation.CASTOR);

        for (final File file : FileUtils.listFiles(new File(configHome, "events"), new String[] { "xml" }, true)) {
            addFile("events/" + file.getName(), Events.class, Implementation.JAXB);
        }
        for (final File file : FileUtils.listFiles(new File(configHome, "datacollection"), new String[] { "xml" }, true)) {
            addFile("datacollection/" + file.getName(), DatacollectionGroup.class, Implementation.JAXB);
        }
    }

    /**
     * Normalize.
     *
     * @throws Exception the exception
     */
    public void normalize() throws Exception {
        for (Source source : configSources) {
            unmarshall(source);
        }
    }

    /**
     * Unmarshall.
     *
     * @param source the source
     */
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

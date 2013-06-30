/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class holds all OpenNMS related config filenames
 */
public final class ConfigFileConstants {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigFileConstants.class);

    private static final String[] FILE_ID_TO_NAME;

    /**
     * The opennms database config file
     */
    public static final int DB_CONFIG_FILE_NAME;

    /**
     * The opennms jms config file
     */
    public static final int JMS_CONFIG_FILE_NAME;

    //
    // the OpenNMS services' config files
    //

    /**
     * The opennms actiond config file
     */
    public static final int ACTIOND_CONFIG_FILE_NAME;

    /**
     * The opennms capsd config file
     */
    public static final int CAPSD_CONFIG_FILE_NAME;

    /**
     * The opennms discovery config file
     */
    public static final int DISCOVERY_CONFIG_FILE_NAME;

    /**
     * The opennms eventd config file
     */
    public static final int EVENTD_CONFIG_FILE_NAME;

    /**
     * The Availability Reports HTML converter xsl file.
     */
    public static final int REPORT_HTML_XSL;

    /**
     * The opennms dhcpd config file
     */
    public static final int DHCPD_CONFIG_FILE_NAME;

    /**
     * The opennms notifd config file
     */
    public static final int NOTIFD_CONFIG_FILE_NAME;

    /**
     * The opennms outage manager config file
     */
    public static final int OUTAGE_MANAGER_CONFIG_FILE_NAME;

    /**
     * The opennms poller config file
     */
    public static final int POLLER_CONFIG_FILE_NAME;

    /**
     * The opennms poll outages config file
     */
    public static final int POLL_OUTAGES_CONFIG_FILE_NAME;

    /** The opennms SNMP poller config file
     */
    public static final int SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME;

    /**
     * The opennms rtc config file
     */
    public static final int RTC_CONFIG_FILE_NAME;

    /**
     * The opennms trapd config file
     */
    public static final int TRAPD_CONFIG_FILE_NAME;

    /**
     * The opennms manager config file
     */
    public static final int MANAGER_CONFIG_FILE_NAME;

    /**
     * The services config file
     */
    public static final int SERVICE_CONF_FILE_NAME;

    /**
     * The events archiver config file
     */
    public static final int EVENTS_ARCHIVER_CONFIG_FILE_NAME;

    /**
     * The opennms collectd config file
     */
    public static final int COLLECTD_CONFIG_FILE_NAME;

    /**
     * The opennms threshd config file
     */
    public static final int THRESHD_CONFIG_FILE_NAME;

    /**
     * The opennms vulnscand config file
     */
    public static final int VULNSCAND_CONFIG_FILE_NAME;

    /**
     * The opennms scriptd config file
     */
    public static final int SCRIPTD_CONFIG_FILE_NAME;

    /**
     * The vacuumd config file
     */
    public static final int VACUUMD_CONFIG_FILE_NAME;

    //

    /**
     * The opennms xmlrpcd config file
     */
    public static final int XMLRPCD_CONFIG_FILE_NAME;

    /**
     * The config file specifying the rrd config (ie JRobin vs JNI)
     */
    public static final int RRD_CONFIG_FILE_NAME;

    /**
     * The config file specifying the JavaMailer config (ie SMTP HOST)
     * 
     */

    public static final int JAVA_MAIL_CONFIG_FILE_NAME;

    /**
     * The config file specifying the XMPP config (host, user, password, etc.)
     * 
     */
    public static final int XMPP_CONFIG_FILE_NAME;
    
    /**
     * JFree Chart configuration file
     */
    public static final int CHART_CONFIG_FILE_NAME;
    
    /**
     * JFree Chart configuration file
     */
    public static final int TRANSLATOR_CONFIG_FILE_NAME;
    
    /**
     * The config file for specifying JMX MBeans
     */
    public static final int JMX_DATA_COLLECTION_CONF_FILE_NAME;

    /**
     * The config file for syslogd
     */
    public static final int SYSLOGD_CONFIG_FILE_NAME;

    //
    // End services config files
    //

    //
    // Other config files
    //

    /**
     * The administrator pitXML file for the Swing GUI
     */
    public static final int ADMIN_PITXML_FILE_NAME;

    /**
     * The "magic" users config file
     */
    public static final int MAGIC_USERS_CONF_FILE_NAME;

    /**
     * The "poller-config" config file
     */
    public static final int POLLER_CONF_FILE_NAME;

    /**
     * The properties file that contains a list of event uies to exclude from
     * the notification wizard
     */
    public static final int EXCLUDE_UEI_FILE_NAME;

    /**
     * The users config file
     */
    public static final int USERS_CONF_FILE_NAME;

    /**
     * The user views config file
     */
    public static final int VIEWS_CONF_FILE_NAME;

    /**
     * The categories config file
     */
    public static final int CATEGORIES_CONF_FILE_NAME;

    /**
     * The user groups config file
     */
    public static final int GROUPS_CONF_FILE_NAME;

    /**
     * The views display config file
     */
    public static final int VIEWS_DISPLAY_CONF_FILE_NAME;

    /**
     * the notifications conf file
     */
    public static final int NOTIFICATIONS_CONF_FILE_NAME;

    /**
     * the notification commands file
     */
    public static final int NOTIF_COMMANDS_CONF_FILE_NAME;

    /**
     * the destination paths for notifications
     */
    public static final int DESTINATION_PATHS_CONF_FILE_NAME;

    /**
     * The SNMP config file
     */
    public static final int SNMP_CONF_FILE_NAME;

    /**
     * the event conf file
     */
    public static final int EVENT_CONF_FILE_NAME;

    /**
     * the database schema file
     */
    public static final int DB_SCHEMA_FILE_NAME;

    /**
     * the SNMP data collection conf file
     */
    public static final int DATA_COLLECTION_CONF_FILE_NAME;

    /**
     * a store for general information needed to configure
     */
    public static final int BASE_CONFIGURATION_FILE_NAME;

    /**
     * the thresholding config file
     */
    public static final int THRESHOLDING_CONF_FILE_NAME;

    /**
     * This is the name of the path of the webui colors dtd
     */
    public static final int WEBUI_COLORS_FILE_NAME;

    /**
     * This is the name of the path of the webui colors dtd
     */
    public static final int KSC_REPORT_FILE_NAME;

    /**
     * The opennms server config file
     */
    public static final int OPENNMS_SERVER_CONFIG_FILE_NAME;

    /**
     * The opennms surveillance views config file
     */
    public static final int SURVEILLANCE_VIEWS_FILE_NAME;

    /**
     * The opennms surveillance views config file
     */
    public static final int SITE_STATUS_VIEWS_FILE_NAME;
    
    /** Constant <code>HTTP_COLLECTION_CONFIG_FILE_NAME</code> */
    public static final int HTTP_COLLECTION_CONFIG_FILE_NAME;

    /**
     * The config file for maps Adapter
     */
    public static final int MAPS_ADAPTER_CONFIG_FILE_NAME;

    //
    // End other config files
    //

    //
    // XSL files used by reports
    //

    /**
     * The reporting pdf xsl file
     */
    public static final int REPORT_PDF_XSL;

    /**
     * The reporting svg xsl file
     */
    public static final int REPORT_SVG_XSL;

    /**
     * The Events reporting xsl file
     */
    public static final int EVENT_REPORT;

    /**
     * The Outages reporting xsl file
     */
    public static final int OUTAGE_REPORT;

    /**
     * The Outages reporting xsl file
     */
    public static final int NOTIFY_REPORT;

    /**
     * The Linkd discovery configuration file
     */
    public static final int LINKD_CONFIG_FILE_NAME;
    
    /**
     * The OpenNMS DataSourceConfiguration file
     */
    public static final int OPENNMS_DATASOURCE_CONFIG_FILE_NAME;
    
    /**
     * The map properties config file
     */
    public static final int MAP_PROPERTIES_FILE_NAME;

    //
    // End XSL files used by reports
    //

    //
    // DTDs used by reports
    //

    /**
     * This is the name of the path of the event report dtd
     */
    public static final int EVENT_REPORT_DTD;

    /**
     * This is the name of the path of the event report dtd
     */
    public static final int OUTAGE_REPORT_DTD;

    /**
     * This is the name of the path of the notification report dtd
     */
    public static final int NOTIFY_REPORT_DTD;

    
    /**
     * The NSClient data collection configuration file
     */
    public static final int NSCLIENT_COLLECTION_CONFIG_FILE_NAME;

    /**
     * The NSClient data collection configuration file
     */
    public static final int NSCLIENT_CONFIG_FILE_NAME;
    
    /**
     * The WMI agent configuration file
     */
    public static final int WMI_CONFIG_FILE_NAME;

    /**
     * The XMP data collection configuration file
     */
    public static final int XMP_COLLECTION_CONFIG_FILE_NAME;

    /**
     * The XMP agent configuration file
     */
    public static final int XMP_CONFIG_FILE_NAME;
    
    /**
     * The RWS (RestFul Web Service) configuration file
     * used to access Rancid
     */
    public static final int RWS_CONFIG_FILE_NAME;   

    /**
     * The Rancid Provisioning Adapter configuration file
     */
    public static final int RANCID_CONFIG_FILE_NAME;

    /**
     * The SNMP Asset Provisioning Adapter configuration file
     */
    public static final int SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME;


    /**
     * The WMI collection configuration file
     */
    public static final int WMI_COLLECTION_CONFIG_FILE_NAME;
    
    /**
     * The Asterisk global configuration file (for notifications, etc.)
     */
    public static final int ASTERISK_CONFIG_FILE_NAME;

    /**
     * The AMI agent configuration file
     */
    public static final int AMI_CONFIG_FILE_NAME;
    
    /**
     * The config file for microblog notifications and acks
     */
    public static final int MICROBLOG_CONFIG_FILE_NAME;
    
    /**
     * The config file for the JDBC Data Collector.
     */
    public static final int JDBC_COLLECTION_CONFIG_FILE_NAME;

    //
    // Initialize the class data. This section is used to initialize the
    // integer constants to their final values and to build the array
    // of integer identifiers to file names.
    //
    static {
        // Initialize the constants
        //

        DB_CONFIG_FILE_NAME = 0;
        JMS_CONFIG_FILE_NAME = 1;
        ACTIOND_CONFIG_FILE_NAME = 2;
        CAPSD_CONFIG_FILE_NAME = 3;
        DISCOVERY_CONFIG_FILE_NAME = 4;

        EVENTD_CONFIG_FILE_NAME = 5;
        NOTIFD_CONFIG_FILE_NAME = 6;
        OUTAGE_MANAGER_CONFIG_FILE_NAME = 7;
        POLLER_CONFIG_FILE_NAME = 8;
        POLL_OUTAGES_CONFIG_FILE_NAME = 9;

        RTC_CONFIG_FILE_NAME = 10;
        TRAPD_CONFIG_FILE_NAME = 11;
        MANAGER_CONFIG_FILE_NAME = 12;
        SERVICE_CONF_FILE_NAME = 13;
        EVENTS_ARCHIVER_CONFIG_FILE_NAME = 14;

        ADMIN_PITXML_FILE_NAME = 15;
        MAGIC_USERS_CONF_FILE_NAME = 16;
        POLLER_CONF_FILE_NAME = 17;
        EXCLUDE_UEI_FILE_NAME = 18;
        USERS_CONF_FILE_NAME = 19;

        VIEWS_CONF_FILE_NAME = 20;
        CATEGORIES_CONF_FILE_NAME = 21;
        GROUPS_CONF_FILE_NAME = 22;
        NOTIFICATIONS_CONF_FILE_NAME = 23;
        NOTIF_COMMANDS_CONF_FILE_NAME = 24;
        DESTINATION_PATHS_CONF_FILE_NAME = 25;

        SNMP_CONF_FILE_NAME = 26;
        EVENT_CONF_FILE_NAME = 27;
        DB_SCHEMA_FILE_NAME = 28;
        DATA_COLLECTION_CONF_FILE_NAME = 29;
        REPORT_PDF_XSL = 30;

        REPORT_SVG_XSL = 31;
        EVENT_REPORT = 32;
        OUTAGE_REPORT = 33;
        EVENT_REPORT_DTD = 34;
        OUTAGE_REPORT_DTD = 35;

        NOTIFY_REPORT_DTD = 36;
        NOTIFY_REPORT = 37;
        COLLECTD_CONFIG_FILE_NAME = 38;
        BASE_CONFIGURATION_FILE_NAME = 39;

        VULNSCAND_CONFIG_FILE_NAME = 40;

        THRESHD_CONFIG_FILE_NAME = 41;
        THRESHOLDING_CONF_FILE_NAME = 42;
        VIEWS_DISPLAY_CONF_FILE_NAME = 43;
        REPORT_HTML_XSL = 44;

        WEBUI_COLORS_FILE_NAME = 45;

        KSC_REPORT_FILE_NAME = 46;
        SCRIPTD_CONFIG_FILE_NAME = 47;

        OPENNMS_SERVER_CONFIG_FILE_NAME = 48;
        XMLRPCD_CONFIG_FILE_NAME = 49;

        DHCPD_CONFIG_FILE_NAME = 50;

        RRD_CONFIG_FILE_NAME = 51;

        JAVA_MAIL_CONFIG_FILE_NAME = 52;
        VACUUMD_CONFIG_FILE_NAME = 53;

        XMPP_CONFIG_FILE_NAME = 54;
        
        CHART_CONFIG_FILE_NAME = 55;

        JMX_DATA_COLLECTION_CONF_FILE_NAME = 56;
        
        TRANSLATOR_CONFIG_FILE_NAME = 57;

        SYSLOGD_CONFIG_FILE_NAME = 58;
        
        LINKD_CONFIG_FILE_NAME = 59;
        
        MAP_PROPERTIES_FILE_NAME = 60;
        
        SURVEILLANCE_VIEWS_FILE_NAME = 61;
        
        SITE_STATUS_VIEWS_FILE_NAME = 62;
        
        HTTP_COLLECTION_CONFIG_FILE_NAME = 64;
        
        NSCLIENT_COLLECTION_CONFIG_FILE_NAME = 65;
        
        NSCLIENT_CONFIG_FILE_NAME = 66;
        
        WMI_CONFIG_FILE_NAME = 67;

        WMI_COLLECTION_CONFIG_FILE_NAME = 68;
        
        OPENNMS_DATASOURCE_CONFIG_FILE_NAME = 69;
        
        RWS_CONFIG_FILE_NAME = 70;

        XMP_COLLECTION_CONFIG_FILE_NAME = 71;
        
        XMP_CONFIG_FILE_NAME = 72;

        SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME = 73;
        
        ASTERISK_CONFIG_FILE_NAME = 74;
        
        AMI_CONFIG_FILE_NAME = 75;
        
        MAPS_ADAPTER_CONFIG_FILE_NAME = 76;
        
        RANCID_CONFIG_FILE_NAME = 77;
        
        MICROBLOG_CONFIG_FILE_NAME = 78;
        
        SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME = 79;
        
        JDBC_COLLECTION_CONFIG_FILE_NAME = 80;
        
        
        // Allocate and build the mapping of identifiers to names
        //
        FILE_ID_TO_NAME = new String[81];

        FILE_ID_TO_NAME[DB_CONFIG_FILE_NAME] = "opennms-database.xml";
        FILE_ID_TO_NAME[JMS_CONFIG_FILE_NAME] = "opennms-jms.xml";
        FILE_ID_TO_NAME[ACTIOND_CONFIG_FILE_NAME] = "actiond-configuration.xml";
        FILE_ID_TO_NAME[CAPSD_CONFIG_FILE_NAME] = "capsd-configuration.xml";
        FILE_ID_TO_NAME[DISCOVERY_CONFIG_FILE_NAME] = "discovery-configuration.xml";

        FILE_ID_TO_NAME[EVENTD_CONFIG_FILE_NAME] = "eventd-configuration.xml";
        FILE_ID_TO_NAME[NOTIFD_CONFIG_FILE_NAME] = "notifd-configuration.xml";
        FILE_ID_TO_NAME[OUTAGE_MANAGER_CONFIG_FILE_NAME] = "outage-configuration.xml";
        FILE_ID_TO_NAME[POLLER_CONFIG_FILE_NAME] = "poller-configuration.xml";
        FILE_ID_TO_NAME[POLL_OUTAGES_CONFIG_FILE_NAME] = "poll-outages.xml";

        FILE_ID_TO_NAME[RTC_CONFIG_FILE_NAME] = "rtc-configuration.xml";
        FILE_ID_TO_NAME[TRAPD_CONFIG_FILE_NAME] = "trapd-configuration.xml";
        FILE_ID_TO_NAME[MANAGER_CONFIG_FILE_NAME] = "manager-configuration.xml";
        FILE_ID_TO_NAME[SERVICE_CONF_FILE_NAME] = "service-configuration.xml";
        FILE_ID_TO_NAME[EVENTS_ARCHIVER_CONFIG_FILE_NAME] = "events-archiver-configuration.xml";

        FILE_ID_TO_NAME[ADMIN_PITXML_FILE_NAME] = "pitXML.xml";
        FILE_ID_TO_NAME[MAGIC_USERS_CONF_FILE_NAME] = "magic-users.properties";
        FILE_ID_TO_NAME[POLLER_CONF_FILE_NAME] = "poller-config.properties";
        FILE_ID_TO_NAME[EXCLUDE_UEI_FILE_NAME] = "exclude-ueis.properties";
        FILE_ID_TO_NAME[USERS_CONF_FILE_NAME] = "users.xml";

        FILE_ID_TO_NAME[VIEWS_CONF_FILE_NAME] = "views.xml";
        FILE_ID_TO_NAME[CATEGORIES_CONF_FILE_NAME] = "categories.xml";
        FILE_ID_TO_NAME[GROUPS_CONF_FILE_NAME] = "groups.xml";
        FILE_ID_TO_NAME[NOTIFICATIONS_CONF_FILE_NAME] = "notifications.xml";
        FILE_ID_TO_NAME[NOTIF_COMMANDS_CONF_FILE_NAME] = "notificationCommands.xml";
        FILE_ID_TO_NAME[DESTINATION_PATHS_CONF_FILE_NAME] = "destinationPaths.xml";

        FILE_ID_TO_NAME[SNMP_CONF_FILE_NAME] = "snmp-config.xml";
        FILE_ID_TO_NAME[EVENT_CONF_FILE_NAME] = "eventconf.xml";
        FILE_ID_TO_NAME[DB_SCHEMA_FILE_NAME] = "database-schema.xml";
        FILE_ID_TO_NAME[DATA_COLLECTION_CONF_FILE_NAME] = "datacollection-config.xml";
        FILE_ID_TO_NAME[REPORT_PDF_XSL] = "PDFAvailReport.xsl";

        FILE_ID_TO_NAME[REPORT_SVG_XSL] = "SVGAvailReport.xsl";
        FILE_ID_TO_NAME[REPORT_HTML_XSL] = "AvailabilityReports.xsl";
        FILE_ID_TO_NAME[EVENT_REPORT] = "EventReport.xsl";
        FILE_ID_TO_NAME[OUTAGE_REPORT] = "OutageReport.xsl";
        FILE_ID_TO_NAME[EVENT_REPORT_DTD] = "eventreport.dtd";
        FILE_ID_TO_NAME[OUTAGE_REPORT_DTD] = "outagereport.dtd";

        FILE_ID_TO_NAME[NOTIFY_REPORT_DTD] = "notifications.dtd";
        FILE_ID_TO_NAME[NOTIFY_REPORT] = "NotifyReport.xsl";

        FILE_ID_TO_NAME[BASE_CONFIGURATION_FILE_NAME] = "baseConfiguration.xml";

        FILE_ID_TO_NAME[COLLECTD_CONFIG_FILE_NAME] = "collectd-configuration.xml";

        FILE_ID_TO_NAME[THRESHD_CONFIG_FILE_NAME] = "threshd-configuration.xml";
        FILE_ID_TO_NAME[THRESHOLDING_CONF_FILE_NAME] = "thresholds.xml";

        FILE_ID_TO_NAME[VULNSCAND_CONFIG_FILE_NAME] = "vulnscand-configuration.xml";
        FILE_ID_TO_NAME[VIEWS_DISPLAY_CONF_FILE_NAME] = "viewsdisplay.xml";

        FILE_ID_TO_NAME[WEBUI_COLORS_FILE_NAME] = "webui-colors.xml";

        FILE_ID_TO_NAME[KSC_REPORT_FILE_NAME] = "ksc-performance-reports.xml";

        FILE_ID_TO_NAME[SCRIPTD_CONFIG_FILE_NAME] = "scriptd-configuration.xml";

        FILE_ID_TO_NAME[OPENNMS_SERVER_CONFIG_FILE_NAME] = "opennms-server.xml";
        FILE_ID_TO_NAME[XMLRPCD_CONFIG_FILE_NAME] = "xmlrpcd-configuration.xml";
        FILE_ID_TO_NAME[DHCPD_CONFIG_FILE_NAME] = "dhcpd-configuration.xml";
        FILE_ID_TO_NAME[RRD_CONFIG_FILE_NAME] = "rrd-configuration.properties";
        FILE_ID_TO_NAME[JAVA_MAIL_CONFIG_FILE_NAME] = "javamail-configuration.properties";
        FILE_ID_TO_NAME[VACUUMD_CONFIG_FILE_NAME] = "vacuumd-configuration.xml";
        FILE_ID_TO_NAME[XMPP_CONFIG_FILE_NAME] = "xmpp-configuration.properties";
        FILE_ID_TO_NAME[CHART_CONFIG_FILE_NAME] = "chart-configuration.xml";
        FILE_ID_TO_NAME[JMX_DATA_COLLECTION_CONF_FILE_NAME] = "jmx-datacollection-config.xml";
        FILE_ID_TO_NAME[TRANSLATOR_CONFIG_FILE_NAME] = "translator-configuration.xml";
        FILE_ID_TO_NAME[SYSLOGD_CONFIG_FILE_NAME] = "syslogd-configuration.xml";
        FILE_ID_TO_NAME[LINKD_CONFIG_FILE_NAME] = "linkd-configuration.xml";
        FILE_ID_TO_NAME[MAP_PROPERTIES_FILE_NAME] = "map.properties";
        FILE_ID_TO_NAME[SURVEILLANCE_VIEWS_FILE_NAME] = "surveillance-views.xml";
        FILE_ID_TO_NAME[SITE_STATUS_VIEWS_FILE_NAME] = "site-status-views.xml";
        FILE_ID_TO_NAME[HTTP_COLLECTION_CONFIG_FILE_NAME] = "http-datacollection-config.xml";
        FILE_ID_TO_NAME[NSCLIENT_COLLECTION_CONFIG_FILE_NAME] = "nsclient-datacollection-config.xml";
        FILE_ID_TO_NAME[NSCLIENT_CONFIG_FILE_NAME] = "nsclient-config.xml";
        FILE_ID_TO_NAME[WMI_CONFIG_FILE_NAME] = "wmi-config.xml";
        FILE_ID_TO_NAME[WMI_COLLECTION_CONFIG_FILE_NAME] = "wmi-datacollection-config.xml";
        FILE_ID_TO_NAME[OPENNMS_DATASOURCE_CONFIG_FILE_NAME] = "opennms-datasources.xml";
        FILE_ID_TO_NAME[RWS_CONFIG_FILE_NAME] = "rws-configuration.xml";
        FILE_ID_TO_NAME[RANCID_CONFIG_FILE_NAME] = "rancid-configuration.xml";
        FILE_ID_TO_NAME[XMP_COLLECTION_CONFIG_FILE_NAME] = "xmp-datacollection-config.xml";
        FILE_ID_TO_NAME[XMP_CONFIG_FILE_NAME] = "xmp-config.xml";
        FILE_ID_TO_NAME[SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME] = "snmp-interface-poller-configuration.xml";
        FILE_ID_TO_NAME[ASTERISK_CONFIG_FILE_NAME] = "asterisk-configuration.properties";
        FILE_ID_TO_NAME[AMI_CONFIG_FILE_NAME] = "ami-config.xml";
        FILE_ID_TO_NAME[MAPS_ADAPTER_CONFIG_FILE_NAME] = "mapsadapter-configuration.xml";
        FILE_ID_TO_NAME[MICROBLOG_CONFIG_FILE_NAME] = "microblog-configuration.xml";
        FILE_ID_TO_NAME[SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME] = "snmp-asset-adapter-configuration.xml";
        FILE_ID_TO_NAME[JDBC_COLLECTION_CONFIG_FILE_NAME] = "jdbc-datacollection-config.xml";
    }

    /**
     * Returns the base name of the identified file as defined by the passed
     * integer value. This name has not yet been resolved and is only the name
     * of the file with no path location information prepended. If the fully
     * qualified name of the file is needed then use
     * {@link #getFile(int) getFile()}to returned the {@link java.io.File File}
     * object. The java File object can be queried to get more detailed
     * information on the file.
     *
     * @param id
     *            The identifier of the desired file.
     * @return The base name of the file that matches the identifier.
     */
    public static final String getFileName(int id) {
        return FILE_ID_TO_NAME[id];
    }

    /**
     * <p>
     * Returns the java {@link java.io.File File}information for the file
     * identified by the passed integer identifier. If the file cannot be
     * located by the search algorithm then an excption is generated.
     * </p>
     *
     * <p>
     * The file is looked for in the <em>etc</em> directory of the OpenNMS
     * home location identified by the System property <em>opennms.home</em>.
     * If the file is not found in the <em>etc</em> directory then an attempt
     * is made to find it in the root OpenNMS directory. If it still cannot be
     * found then a {@link java.io.FileNotFoundException FileNotFoundException}
     * is generated by the method.
     * </p>
     *
     * @param id
     *            The identifer for the configuration file.
     * @return The File handle to the passed identifier.
     * @throws java.io.FileNotFoundException
     *             Thrown if the file cannot be located.
     * @throws java.io.IOException
     *             Thrown if an error occurs accessing the file system.
     */
    public static final File getFile(int id) throws IOException {
        // Recover the home directory from the system properties.
        String home = getHome();

        // Check to make sure that the home directory exists
        File fhome = new File(home);
        if (!fhome.exists()) {
        	LOG.debug("The specified home directory does not exist.");
            throw new FileNotFoundException("The OpenNMS home directory \"" + home + "\" does not exist");
        }

        String rfile = getFileName(id);
        File frfile = new File(home + File.separator + "etc" + File.separator + rfile);
        if (!frfile.exists()) {
            File frfileNoEtc = new File(home + File.separator + rfile);
            if (!frfileNoEtc.exists()) {
                throw new FileNotFoundException("The requested file '" + rfile
                                                + "' could not be found at '"
                                                + frfile.getAbsolutePath()
                                                + "' or '"
                                                + frfileNoEtc.getAbsolutePath()
                                                + "'");
            }
        }

        return frfile;
    }

    /**
     * <p>
     * Returns the java {@link java.io.File File}information for the file
     * identified by the passed base file name. If the file cannot be located by
     * the search algorithm then an excption is generated.
     * </p>
     *
     * <p>
     * The file is looked for in the <em>etc</em> directory of the OpenNMS
     * home location identified by the System property <em>opennms.home</em>.
     * If the file is not found in the <em>etc</em> directory then an attempt
     * is made to find it in the root OpenNMS directory. If it still cannot be
     * found then a {@link java.io.FileNotFoundException FileNotFoundException}
     * is generated by the method.
     * </p>
     *
     * @param fname
     *            The base file name of the configuration file.
     * @return The File handle to the named file.
     * @throws java.io.FileNotFoundException
     *             Thrown if the file cannot be located.
     * @throws java.io.IOException
     *             Thrown if an error occurs accessing the file system.
     */
    public static final File getConfigFileByName(String fname) throws IOException {
        // Recover the home directory from the system properties.
        //
        String home = getHome();

        // Check to make sure that the home directory exists
        //
        File fhome = new File(home);
        if (!fhome.exists()) {
        	LOG.debug("The specified home directory does not exist.");
            throw new FileNotFoundException("The OpenNMS home directory \"" + home + "\" does not exist.");
        }

        File frfile = new File(home + File.separator + "etc" + File.separator + fname);
        if (!frfile.exists()) {
            File frfileNoEtc = new File(home + File.separator + fname);
            if (!frfileNoEtc.exists()) {
                throw new FileNotFoundException(String.format("The requested file '%s' could not be found at '%s' or '%s'", fname, frfile.getAbsolutePath(), frfileNoEtc.getAbsolutePath()));
            }
        }

        return frfile;
    }

    /**
     * <p>getHome</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static final String getHome() {
        String home = System.getProperty("opennms.home");
        if (home == null) {
        	LOG.debug("The 'opennms.home' property was not set, falling back to /opt/opennms.  This should really only happen in unit tests.");
            home = File.separator + "opt" + File.separator + "opennms";
        }
        // Remove the trailing slash if necessary
        //
        if (home.endsWith("/") || home.endsWith(File.separator))
            home = home.substring(0, home.length() - 1);

        return home;
    }

    /**
     * Method to return the string for the managed address include files for
     * discovery, capsd and polling.
     *
     * @return String, the file url for the include file
     */
    public static final String getIncludeFileString() {
        return "file:" + getHome() + File.separator + "etc" + File.separator + "include";
    }

    /**
     * Method to return the string for path of the etc directory.
     *
     * @return String, the file url for the include file
     */
    public static final String getFilePathString() {
        return getHome() + File.separator + "etc" + File.separator;
    }

    /**
     * <p>getTimezoneFileDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static final String getTimezoneFileDir() {
        return File.separator + "usr" + File.separator + "share" + File.separator + "zoneinfo" + File.separator + "US";
    }

    /** Constant <code>RRD_DS_MAX_SIZE=19</code> */
    public static final int RRD_DS_MAX_SIZE = 19;
}

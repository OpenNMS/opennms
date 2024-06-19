/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * This class holds all OpenNMS related config filenames
 */
public abstract class ConfigFileConstants {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigFileConstants.class);

    public static final String ACTIOND_CONFIG_FILE_NAME = "actiond-configuration.xml";
    public static final String DISCOVERY_CONFIG_FILE_NAME = "discovery-configuration.xml";

    public static final String EVENTD_CONFIG_FILE_NAME = "eventd-configuration.xml";
    public static final String NOTIFD_CONFIG_FILE_NAME = "notifd-configuration.xml";
    public static final String POLLER_CONFIG_FILE_NAME = "poller-configuration.xml";
    public static final String POLL_OUTAGES_CONFIG_FILE_NAME = "poll-outages.xml";

    public static final String RTC_CONFIG_FILE_NAME = "rtc-configuration.xml";
    public static final String TRAPD_CONFIG_FILE_NAME = "trapd-configuration.xml";
    public static final String SERVICE_CONF_FILE_NAME = "service-configuration.xml";

    public static final String EXCLUDE_UEI_FILE_NAME = "exclude-ueis.properties";
    public static final String USERS_CONF_FILE_NAME = "users.xml";

    public static final String CATEGORIES_CONF_FILE_NAME = "categories.xml";
    public static final String GROUPS_CONF_FILE_NAME = "groups.xml";
    public static final String NOTIFICATIONS_CONF_FILE_NAME = "notifications.xml";
    public static final String NOTIF_COMMANDS_CONF_FILE_NAME = "notificationCommands.xml";
    public static final String DESTINATION_PATHS_CONF_FILE_NAME = "destinationPaths.xml";

    public static final String SNMP_CONF_FILE_NAME = "snmp-config.xml";
    public static final String EVENT_CONF_FILE_NAME = "eventconf.xml";
    public static final String DATA_COLLECTION_CONF_FILE_NAME = "datacollection-config.xml";

    public static final String COLLECTD_CONFIG_FILE_NAME = "collectd-configuration.xml";

    public static final String THRESHD_CONFIG_FILE_NAME = "threshd-configuration.xml";
    public static final String THRESHOLDING_CONF_FILE_NAME = "thresholds.xml";

    public static final String VIEWS_DISPLAY_CONF_FILE_NAME = "viewsdisplay.xml";
    public static final String RRD_CONFIG_FILE_NAME = "rrd-configuration.properties";
    public static final String JAVA_MAIL_CONFIG_FILE_NAME = "javamail-configuration.properties";
    public static final String VACUUMD_CONFIG_FILE_NAME = "vacuumd-configuration.xml";
    public static final String XMPP_CONFIG_FILE_NAME = "xmpp-configuration.properties";
    public static final String JMX_DATA_COLLECTION_CONF_FILE_NAME = "jmx-datacollection-config.xml";
    public static final String TRANSLATOR_CONFIG_FILE_NAME = "translator-configuration.xml";
    public static final String SYSLOGD_CONFIG_FILE_NAME = "syslogd-configuration.xml";
    public static final String SYSLOGD_GROK_PATTERNS_FILE_NAME = "syslogd-grok-patterns.txt";
    public static final String ENLINKD_CONFIG_FILE_NAME = "enlinkd-configuration.xml";
    public static final String SURVEILLANCE_VIEWS_FILE_NAME = "surveillance-views.xml";
    public static final String SITE_STATUS_VIEWS_FILE_NAME = "site-status-views.xml";
    public static final String HTTP_COLLECTION_CONFIG_FILE_NAME = "http-datacollection-config.xml";
    public static final String NSCLIENT_COLLECTION_CONFIG_FILE_NAME = "nsclient-datacollection-config.xml";
    public static final String NSCLIENT_CONFIG_FILE_NAME = "nsclient-config.xml";
    public static final String WMI_CONFIG_FILE_NAME = "wmi-config.xml";
    public static final String WMI_COLLECTION_CONFIG_FILE_NAME = "wmi-datacollection-config.xml";
    public static final String OPENNMS_DATASOURCE_CONFIG_FILE_NAME = "opennms-datasources.xml";
    public static final String SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME = "snmp-interface-poller-configuration.xml";
    public static final String ASTERISK_CONFIG_FILE_NAME = "asterisk-configuration.properties";
    public static final String AMI_CONFIG_FILE_NAME = "ami-config.xml";
    public static final String MICROBLOG_CONFIG_FILE_NAME = "microblog-configuration.xml";
    public static final String SNMP_ASSET_ADAPTER_CONFIG_FILE_NAME = "snmp-asset-adapter-configuration.xml";
    public static final String WSMAN_ASSET_ADAPTER_CONFIG_FILE_NAME = "wsman-asset-adapter-configuration.xml";
    public static final String JDBC_COLLECTION_CONFIG_FILE_NAME = "jdbc-datacollection-config.xml";

    /**
     * Constant <code>RRD_DS_MAX_SIZE=19</code>
     */
    public static final int RRD_DS_MAX_SIZE = 19;

    /**
     * @deprecated use the provided <code>filename</code> instead. See all available constants in {@link ConfigFileConstants}
     */
    @Deprecated(forRemoval = true)
    public static String getFileName(String filename) {
        return filename;
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
     * @param fname The base file name of the configuration file.
     * @return The File handle to the named file.
     * @throws java.io.FileNotFoundException Thrown if the file cannot be located.
     * @throws java.io.IOException           Thrown if an error occurs accessing the file system.
     */
    public static File getConfigFileByName(String fname) throws IOException {
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
     * Alias for {@link #getConfigFileByName(String)}
     *
     * @see #getConfigFileByName(String) 
     */
    public static File getFile(String filename) throws IOException {
        return getConfigFileByName(filename);
    }

    public static String getHome() {
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
     * Method to return the string for path of the etc directory.
     *
     * @return String, the file url for the include file
     */
    public static String getFilePathString() {
        return getHome() + File.separator + "etc" + File.separator;
    }

}

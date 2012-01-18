/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.SyslogdConfiguration;
import org.opennms.netmgt.config.syslogd.SyslogdConfigurationGroup;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Syslogd from syslogd-configuration.xml. <strong>Note: </strong>Users of
 * this class should make sure the <em>init()</em> is called before calling
 * any other method to ensure the config is loaded before accessing other
 * convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class SyslogdConfigFactory implements SyslogdConfig {
    /**
     * The singleton instance of this factory
     */
    private static SyslogdConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private SyslogdConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     * @throws org.exolab.castor.xml.MarshalException
     *                             Thrown if the file does not conform to the schema.
     * @throws org.exolab.castor.xml.ValidationException
     *                             Thrown if the contents do not match the required schema.
     */
    private SyslogdConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(SyslogdConfiguration.class, new FileSystemResource(configFile));
        parseIncludedFiles();
    }

    /**
     * <p>Constructor for SyslogdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public SyslogdConfigFactory(InputStream stream) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(SyslogdConfiguration.class, stream);
        parseIncludedFiles();
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     * @throws org.exolab.castor.xml.MarshalException
     *                             Thrown if the file does not conform to the schema.
     * @throws org.exolab.castor.xml.ValidationException
     *                             Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException,
            MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_CONFIG_FILE_NAME);

        m_singleton = new SyslogdConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be
     *                             read/loaded
     * @throws org.exolab.castor.xml.MarshalException
     *                             Thrown if the file does not conform to the schema.
     * @throws org.exolab.castor.xml.ValidationException
     *                             Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException,
            MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *          Thrown if the factory has not yet been initialized.
     */
    public static synchronized SyslogdConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException(
                    "The factory has not been initialized");

        return m_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param config a {@link org.opennms.netmgt.config.SyslogdConfig} object.
     */
    public static synchronized void setInstance(SyslogdConfig config) {
        m_singleton = config;
        m_loaded = true;
    }

    /**
     * Return the port on which SNMP traps should be received.
     *
     * @return the port on which SNMP traps should be received
     */
    public synchronized int getSyslogPort() {
        return m_config.getConfiguration().getSyslogPort();
    }

    /**
     * <p>getListenAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.8.1
     */
    public synchronized String getListenAddress() {
        return m_config.getConfiguration().getListenAddress();
    }
    
    /**
     * Return whether or not a newSuspect event should be sent when a trap is
     * received from an unknown IP address.
     *
     * @return whether to generate newSuspect events on traps.
     */
    public synchronized boolean getNewSuspectOnMessage() {
        return m_config.getConfiguration().getNewSuspectOnMessage();
    }

    /**
     * <p>getForwardingRegexp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public synchronized String getForwardingRegexp() {
        return m_config.getConfiguration().getForwardingRegexp();
    }

    /**
     * <p>getMatchingGroupHost</p>
     *
     * @return a int.
     */
    public synchronized int getMatchingGroupHost() {
        return m_config.getConfiguration().getMatchingGroupHost();

    }

    /**
     * <p>getMatchingGroupMessage</p>
     *
     * @return a int.
     */
    public synchronized int getMatchingGroupMessage() {
        return m_config.getConfiguration().getMatchingGroupMessage();

    }

    /**
     * <p>getParser</p>
     *
     * @return the parser class to use when parsing syslog messages, as a string.
     */
    public synchronized String getParser() {
        return m_config.getConfiguration().getParser();
    }

    /**
     * <p>getUeiList</p>
     *
     * @return a {@link org.opennms.netmgt.config.syslogd.UeiList} object.
     */
    public synchronized UeiList getUeiList() {
        return m_config.getUeiList();
    }

    /**
     * <p>getHideMessages</p>
     *
     * @return a {@link org.opennms.netmgt.config.syslogd.HideMessage} object.
     */
    public synchronized HideMessage getHideMessages() {
        return m_config.getHideMessage();
    }
    
    /**
     * <p>getDiscardUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public synchronized String getDiscardUei() {
        return m_config.getConfiguration().getDiscardUei();
    }

    /**
     * Parse import-file tags and add all uei-matchs and hide-messages.
     * 
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    private void parseIncludedFiles() throws IOException, MarshalException, ValidationException {
        final File configDir;
        try {
            configDir = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_CONFIG_FILE_NAME).getParentFile();
        } catch (final Throwable t) {
            LogUtils.warnf(this, "Error getting default syslogd configuration location. <import-file> directives will be ignored.  This should really only happen in unit tests.");
            return;
        }
        for (final String fileName : m_config.getImportFileCollection()) {
            final File configFile = new File(configDir, fileName);
            final SyslogdConfigurationGroup includeCfg = CastorUtils.unmarshal(SyslogdConfigurationGroup.class, new FileSystemResource(configFile));
            if (includeCfg.getUeiList() != null) {
                for (final UeiMatch ueiMatch : includeCfg.getUeiList().getUeiMatchCollection())  {
                    if (m_config.getUeiList() == null)
                        m_config.setUeiList(new UeiList());
                    m_config.getUeiList().addUeiMatch(ueiMatch);
                }
            }
            if (includeCfg.getHideMessage() != null) {
                for (final HideMatch hideMatch : includeCfg.getHideMessage().getHideMatchCollection()) {
                    if (m_config.getHideMessage() == null)
                        m_config.setHideMessage(new HideMessage());
                    m_config.getHideMessage().addHideMatch(hideMatch);
                }
            }
        }
    }
}

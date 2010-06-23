//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 08: Associate nodeid with traps based on IP address if possible.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.SyslogdConfiguration;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.dao.castor.CastorUtils;
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
    private SyslogdConfigFactory(String configFile) throws IOException,
            MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(SyslogdConfiguration.class, new FileSystemResource(configFile));
    }

    @Deprecated
    public SyslogdConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(SyslogdConfiguration.class, rdr);
    }

    public SyslogdConfigFactory(InputStream stream) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(SyslogdConfiguration.class, stream);
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

    public synchronized String getForwardingRegexp() {
        return m_config.getConfiguration().getForwardingRegexp();
    }

    public synchronized int getMatchingGroupHost() {
        return m_config.getConfiguration().getMatchingGroupHost();

    }

    public synchronized int getMatchingGroupMessage() {
        return m_config.getConfiguration().getMatchingGroupMessage();

    }

    public synchronized UeiList getUeiList() {
        return m_config.getUeiList();
    }

    public synchronized HideMessage getHideMessages() {
        return m_config.getHideMessage();
    }
    
    public synchronized String getDiscardUei() {
        return m_config.getConfiguration().getDiscardUei();
    }

}

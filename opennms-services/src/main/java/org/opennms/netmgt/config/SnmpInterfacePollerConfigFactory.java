//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 14: Create a logStatic method and use setInstance to save the instance in init() - dj@opennms.org
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Jan 31: Added code to allow for RRA definitions within poller packages.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Poller service from the poller-configuration XML file.
 * 
 * A mapping of the configured URLs to the IP list they contain is built at
 * init() time so as to avoid numerous file reads.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class SnmpInterfacePollerConfigFactory extends SnmpInterfacePollerConfigManager {
    /**
     * The singleton instance of this factory
     */
    private static SnmpInterfacePollerConfig m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    @Deprecated
    public SnmpInterfacePollerConfigFactory(long currentVersion, Reader reader, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        super(reader, localServer, verifyServer);
        m_currentVersion = currentVersion;
    }

    public SnmpInterfacePollerConfigFactory(long currentVersion, InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
        super(stream, localServer, verifyServer);
        m_currentVersion = currentVersion;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        OpennmsServerConfigFactory.init();
        OpennmsServerConfigFactory onmsSvrConfig = OpennmsServerConfigFactory.getInstance();

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME);

        logStatic().debug("init: config file path: " + cfgFile.getPath());

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            SnmpInterfacePollerConfigFactory config = new SnmpInterfacePollerConfigFactory(cfgFile.lastModified(), stream, onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer());
            setInstance(config);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private static ThreadCategory logStatic() {
        return ThreadCategory.getInstance(SnmpInterfacePollerConfigFactory.class);
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        init();
        getInstance().update();
    }

    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME);
            logStatic().debug("saveXml: saving config file at "+timestamp+": " + cfgFile.getPath());
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            logStatic().debug("saveXml: finished saving config file: " + cfgFile.getPath());
        }
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized SnmpInterfacePollerConfig getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    public static synchronized void setInstance(SnmpInterfacePollerConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    public synchronized void update() throws IOException, MarshalException, ValidationException {

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SNMP_INTERFACE_POLLER_CONFIG_FILE_NAME);
        if (cfgFile.lastModified() > m_currentVersion) {
            m_currentVersion = cfgFile.lastModified();
            logStatic().debug("init: config file path: " + cfgFile.getPath());
            InputStream stream = null;
            try {
                stream = new FileInputStream(cfgFile);
                reloadXML(stream);
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }
            logStatic().debug("init: finished loading config file: " + cfgFile.getPath());
        }
    }
}

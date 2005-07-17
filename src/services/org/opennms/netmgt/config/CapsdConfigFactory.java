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
// 2004 Dec 27: Updated code to determine primary SNMP interface to select
//              an interface from collectd-configuration.xml first, and if
//              none found, then from all interfaces on the node. In either
//              case, a loopback interface is preferred if available.
// 2004 Jan 06: Added support for STATUS_SUSPEND abd STATUS_RESUME
// 2003 Nov 11: Merged changes from Rackspace project
// 2003 Sep 17: Fixed an SQL parameter problem.
// 2003 Sep 16: Changed rescan information to let OpenNMS handle duplicate IPs.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Aug 27: Fixed <range> tag. Bug #655
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Capsd service from the capsd-configuration xml file.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class CapsdConfigFactory extends CapsdConfigManager {
    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The singleton instance of this factory
     */
    private static CapsdConfig m_singleton = null;

    /**
     * Constructs a new CapsdConfigFactory object for access to the Capsd
     * configuration information.
     * 
     * In addition to loading the configuration from the capsd-configuration.xml
     * file the constructor also inserts any plugins defined in the
     * configuration but not in the database into the 'service' table.
     * @param configFile
     *            The configuration file to load.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private CapsdConfigFactory() throws IOException, MarshalException, ValidationException {
        update();

    }

    protected void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
        ThreadCategory.getInstance(CapsdConfigFactory.class).debug("init: config file path: " + configFile.getPath());
        Reader rdr = new FileReader(configFile);
        loadXml(rdr);
        rdr.close();
    }

    /**
     * Constructs a new CapsdConfigFactory object for access to the Capsd
     * configuration information.
     * 
     * In addition to loading the configuration from the capsd-configuration.xml
     * file the constructor also inserts any plugins defined in the
     * configuration but not in the database into the 'service' table.
     * 
     * @param configFile
     *            The configuration file to load.
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public CapsdConfigFactory(Reader rdr) throws IOException, MarshalException, ValidationException {
        super(rdr);
    }

    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
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

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(CapsdConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new CapsdConfigFactory();

        m_loaded = true;
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
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized CapsdConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }
    
    public static synchronized void setInstance(CapsdConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

}

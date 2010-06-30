/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: Created this file with the non-static pieces of
 *              CapsdConfigFactory. - dj@opennms.org
 * 2004 Dec 27: Updated code to determine primary SNMP interface to select
 *              an interface from collectd-configuration.xml first, and if
 *              none found, then from all interfaces on the node. In either
 *              case, a loopback interface is preferred if available.
 * 2004 Jan 06: Added support for STATUS_SUSPEND abd STATUS_RESUME
 * 2003 Nov 11: Merged changes from Rackspace project
 * 2003 Sep 17: Fixed an SQL parameter problem.
 * 2003 Sep 16: Changed rescan information to let OpenNMS handle duplicate IPs.
 * 2003 Jan 31: Cleaned up some unused imports.
 * 2002 Aug 27: Fixed <range> tag. Bug #655
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * <p>DefaultCapsdConfigManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCapsdConfigManager extends CapsdConfigManager {
    /**
     * Timestamp of the file for the currently loadded configuration
     */
    private long m_currentVersion = -1L;

    /**
     * <p>Constructor for DefaultCapsdConfigManager.</p>
     */
    public DefaultCapsdConfigManager() {
        super();
    }
  
    /**
     * <p>Constructor for DefaultCapsdConfigManager.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public DefaultCapsdConfigManager(Reader rdr) throws MarshalException, ValidationException {
        super(rdr);
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected synchronized void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
        
        log().debug("Checking to see if capsd configuration should be reloaded from " + configFile);
        
        if (m_currentVersion < configFile.lastModified()) {
            log().debug("Reloading capsd configuration file");
            
            long lastModified = configFile.lastModified();
            
            Reader rdr = new FileReader(configFile);
            try {
                loadXml(rdr);
            } finally {
                IOUtils.closeQuietly(rdr);
            }
            
            // Update currentVersion after we have successfully reloaded
            m_currentVersion = lastModified; 

            log().info("Reloaded capsd configuration file");
        }
    }

    /** {@inheritDoc} */
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
            FileWriter fileWriter = new FileWriter(cfgFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;

public class DefaultCapsdConfigManager extends CapsdConfigManager {
    /**
     * Timestamp of the file for the currently loaded configuration
     */
    private long m_currentVersion = -1L;

    public DefaultCapsdConfigManager() {
        super();
    }
  
    @Deprecated
    public DefaultCapsdConfigManager(Reader rdr) throws MarshalException, ValidationException {
        super(rdr);
    }

    public DefaultCapsdConfigManager(InputStream is) throws MarshalException, ValidationException {
        super(is);
    }

    protected synchronized void update() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
        
        log().debug("Checking to see if capsd configuration should be reloaded from " + configFile);
        
        if (m_currentVersion < configFile.lastModified()) {
            log().debug("Reloading capsd configuration file");
            
            long lastModified = configFile.lastModified();

            InputStream is = null;
            try {
                is = new FileInputStream(configFile);
                loadXml(is);
            } finally {
                if (is != null) {
                    IOUtils.closeQuietly(is);
                }
            }
            
            // Update currentVersion after we have successfully reloaded
            m_currentVersion = lastModified; 

            log().info("Reloaded capsd configuration file");
        }
    }

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
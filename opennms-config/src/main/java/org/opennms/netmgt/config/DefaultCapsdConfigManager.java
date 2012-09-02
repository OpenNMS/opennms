/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>DefaultCapsdConfigManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCapsdConfigManager extends CapsdConfigManager {
    /**
     * Timestamp of the file for the currently loaded configuration
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
     * @param is a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public DefaultCapsdConfigManager(InputStream is) throws MarshalException, ValidationException {
        super(is);
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

    /** {@inheritDoc} */
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME);
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}

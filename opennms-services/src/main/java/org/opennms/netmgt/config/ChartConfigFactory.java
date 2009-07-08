/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;

/**
 * @author david
 *
 */
public class ChartConfigFactory extends ChartConfigManager {
    
    private static boolean m_initialized = false;
    private static ChartConfigFactory m_instance = null;
    private static File m_chartConfigFile;
    private static long m_lastModified;
    
    public static synchronized void init() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        if (!m_initialized) {
            m_instance = new ChartConfigFactory();
            reload();
            m_initialized = true;
        }   
    }
    
    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws MarshalException
     * @throws ValidationException
     */
    public static synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
        m_chartConfigFile = ConfigFileConstants.getFile(ConfigFileConstants.CHART_CONFIG_FILE_NAME);

        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_chartConfigFile);
            m_lastModified = m_chartConfigFile.lastModified();
            parseXml(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /**
     * @param xml
     * @throws IOException
     */
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            FileWriter fileWriter = new FileWriter(m_chartConfigFile);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * 
     */
    public void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_chartConfigFile.lastModified()) {
            NotifdConfigFactory.getInstance().reload();
        }
    }

    public static ChartConfigFactory getInstance() {
        if (!m_initialized) {
            throw new IllegalStateException("Factory not initialized");
        }
        
        return m_instance;
    }
    
    public static void setInstance(ChartConfigFactory instance) {
        m_instance = instance;
        m_initialized = true;
    }

}

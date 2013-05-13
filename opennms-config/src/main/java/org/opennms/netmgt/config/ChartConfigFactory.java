/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

/**
 * <p>ChartConfigFactory class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class ChartConfigFactory extends ChartConfigManager {
    
    private static boolean m_initialized = false;
    private static ChartConfigFactory m_instance = null;
    private static File m_chartConfigFile;
    private static long m_lastModified;
    
    /**
     * <p>init</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        if (!m_initialized) {
            m_instance = new ChartConfigFactory();
            reload();
            m_initialized = true;
        }   
    }
    
    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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

    /** {@inheritDoc} */
    @Override
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_chartConfigFile), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Override
    public void update() throws IOException, MarshalException, ValidationException {
        if (m_lastModified != m_chartConfigFile.lastModified()) {
            NotifdConfigFactory.getInstance().reload();
        }
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.config.ChartConfigFactory} object.
     */
    public static ChartConfigFactory getInstance() {
        if (!m_initialized) {
            throw new IllegalStateException("Factory not initialized");
        }
        
        return m_instance;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.ChartConfigFactory} object.
     */
    public static void setInstance(ChartConfigFactory instance) {
        m_instance = instance;
        m_initialized = true;
    }

}

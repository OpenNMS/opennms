/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;

/**
 * A factory for creating XmlDataCollectionConfig objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigFactory {

    /** The Constant XML_DATACOLLECTION_CONFIG_FILE. */
    public static final String XML_DATACOLLECTION_CONFIG_FILE = "xml-datacollection-config.xml";
    
    /** The XML data collection configuration. */
    private XmlDataCollectionConfig m_xmlDataCollectionConfig = null;

    /**
     * Instantiates a new XML data collection configuration factory.
     */
    public XmlDataCollectionConfigFactory() {
        try {
            File cfgFile = ConfigFileConstants.getConfigFileByName(XML_DATACOLLECTION_CONFIG_FILE);
            log().debug("init: config file path: " + cfgFile.getPath());
            InputStream reader = new FileInputStream(cfgFile);
            unmarshall(reader);
            reader.close();
        } catch(IOException e) {
            log().error(e.getMessage(), e); // TODO
        }
    }

    /**
     * Unmarshall.
     *
     * @param configFile the configuration file
     * @return the XML data collection configuration
     */
    public XmlDataCollectionConfig unmarshall(InputStream configFile) {
        try {
            InputStream jdccStream = configFile;
            JAXBContext context = JAXBContext.newInstance(XmlDataCollectionConfig.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setSchema(null);
            XmlDataCollectionConfig jdcc = (XmlDataCollectionConfig) um.unmarshal(jdccStream);
            m_xmlDataCollectionConfig = jdcc;
            return jdcc;
        } catch (Throwable e) {
            log().error(e.getMessage(), e); // TODO
        }
        return m_xmlDataCollectionConfig;
    }

    /**
     * Log.
     *
     * @return the thread category
     */
    protected static ThreadCategory log() {
        return ThreadCategory.getInstance(XmlDataCollectionConfig.class);
    }

}

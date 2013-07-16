/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.dao.jaxb;

import java.io.File;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlGroups;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class XmlDataCollectionConfigDaoJaxb.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<XmlDataCollectionConfig, XmlDataCollectionConfig> implements XmlDataCollectionConfigDao {

    private static final Logger LOG = LoggerFactory.getLogger(XmlDataCollectionConfigDaoJaxb.class);

    /**
     * Instantiates a new XML data collection configuration DAO using JAXB.
     */
    public XmlDataCollectionConfigDaoJaxb() {
        super(XmlDataCollectionConfig.class, "XML Data Collection Configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByName(java.lang.String)
     */
    @Override
    public XmlDataCollection getDataCollectionByName(String name) {
        XmlDataCollectionConfig config = getContainer().getObject();
        for (XmlDataCollection dataCol : config.getXmlDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByIndex(int)
     */
    @Override
    public XmlDataCollection getDataCollectionByIndex(int idx) {
        XmlDataCollectionConfig config = getContainer().getObject();
        return config.getXmlDataCollections().get(idx);
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getConfig()
     */
    @Override
    public XmlDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.jaxb.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    protected XmlDataCollectionConfig translateConfig(XmlDataCollectionConfig config) {
        for (XmlDataCollection collection : config.getXmlDataCollections()) {
            for (XmlSource source : collection.getXmlSources()) {
                parseXmlGroups(source);
            }
        }
        return config;
    }

    /**
     * Parses the XML groups.
     *
     * @param source the XML source
     */
    private void parseXmlGroups(XmlSource source) {
        if (!source.hasImportGroups()) {
            return;
        }
        for (String importGroup : source.getImportGroupsList()) {
            File file = new File(ConfigFileConstants.getHome(), "/etc/" + importGroup);
            LOG.debug("parseXmlGroups: parsing {}", file);
            XmlGroups groups = JaxbUtils.unmarshal(XmlGroups.class, new FileSystemResource(file));
            source.getXmlGroups().addAll(groups.getXmlGroups());
        }
    }

}

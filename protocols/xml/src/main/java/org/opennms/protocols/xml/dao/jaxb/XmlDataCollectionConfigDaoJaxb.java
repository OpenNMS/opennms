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

package org.opennms.protocols.xml.dao.jaxb;

import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;

/**
 * The Class XmlDataCollectionConfigDaoJaxb.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlDataCollectionConfigDaoJaxb extends AbstractJaxbConfigDao<XmlDataCollectionConfig, XmlDataCollectionConfig> implements XmlDataCollectionConfigDao {

    /**
     * Instantiates a new XML data collection configuration DAO using JAXB.
     */
    public XmlDataCollectionConfigDaoJaxb() {
        super(XmlDataCollectionConfig.class, "XML Data Collection Configuration");
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByName(java.lang.String)
     */
    public XmlDataCollection getDataCollectionByName(String name) {
        XmlDataCollectionConfig xmlcc = getContainer().getObject();
        for (XmlDataCollection dataCol : xmlcc.getXmlDataCollections()) {
            if(dataCol.getName().equals(name)) {
                return dataCol;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getDataCollectionByIndex(int)
     */
    public XmlDataCollection getDataCollectionByIndex(int idx) {
        XmlDataCollectionConfig jdcc = getContainer().getObject();
        return jdcc.getXmlDataCollections().get(idx);
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao#getConfig()
     */
    public XmlDataCollectionConfig getConfig() {
        return getContainer().getObject();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.dao.jaxb.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    public XmlDataCollectionConfig translateConfig(XmlDataCollectionConfig jaxbConfig) {
        return jaxbConfig;
    }

}

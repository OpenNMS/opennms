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

package org.opennms.protocols.xml.config;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.protocols.xml.collector.XmlResourceType;
import org.opennms.protocols.xml.collector.XmlStorageStrategy;

/**
 * The XML Resource Utilities.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlResourceUtils {


    /**
     * Instantiates a new XML resource Utils.
     */
    private XmlResourceUtils() {}

    /**
     * Gets the XML resource type.
     *
     * @param agent the collection agent
     * @param resourceType the resource type
     * @return the XML resource type
     */
    public static XmlResourceType getXmlResourceType(CollectionAgent agent, String resourceType) {
        ResourceType  rt = new ResourceType();
        rt.setName(resourceType);
        rt.setStorageStrategy(new StorageStrategy());
        rt.getStorageStrategy().setClazz(XmlStorageStrategy.class.getName());
        rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
        rt.getPersistenceSelectorStrategy().setClazz(PersistAllSelectorStrategy.class.getName());
        XmlResourceType type = new XmlResourceType(agent, rt);
        return type;
    }
}


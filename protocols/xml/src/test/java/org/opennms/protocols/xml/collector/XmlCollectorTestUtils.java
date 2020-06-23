/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.protocols.xml.config.XmlDataCollection;

public class XmlCollectorTestUtils {

    public static CollectionSet doCollect(XmlCollector collector, CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        ResourceTypeMapper.getInstance().setResourceTypeMapper(type -> getResourceType(type));
        final Map<String, Object> runtimeAttributes = collector.getRuntimeAttributes(agent, parameters);
        Map<String, Object> allParams = new HashMap<>();
        allParams.putAll(parameters);
        allParams.putAll(runtimeAttributes);
        allParams = Collections.unmodifiableMap(allParams);
        return collector.collect(agent, allParams);
    }

    public static CollectionSet doCollect(NodeDao nodeDao, XmlCollectionHandler handler, CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        ResourceTypeMapper.getInstance().setResourceTypeMapper(type -> getResourceType(type));
        XmlCollector collector = new XmlCollector();
        collector.setNodeDao(nodeDao);
        XmlDataCollection parsedCollection = collector.parseCollection(collection, handler, agent, parameters);
        return handler.collect(agent, parsedCollection, parameters);
    }

    public static ResourceType getResourceType(String resourceType) {
        ResourceType rt = new ResourceType();
        rt.setName(resourceType);
        rt.setStorageStrategy(new StorageStrategy());
        rt.getStorageStrategy().setClazz(XmlStorageStrategy.class.getName());
        rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
        rt.getPersistenceSelectorStrategy().setClazz(PersistAllSelectorStrategy.class.getName());
        return rt;
    }
}

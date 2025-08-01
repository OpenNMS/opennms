/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.xml.collector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Interpolator;
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
        final Map<String, Object> runtimeAttributes = Interpolator.interpolateAttributes(collector.getRuntimeAttributes(agent, parameters), EmptyScope.EMPTY);
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

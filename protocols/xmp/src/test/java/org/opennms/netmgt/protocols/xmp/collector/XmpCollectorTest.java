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

package org.opennms.netmgt.protocols.xmp.collector;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;

public class XmpCollectorTest {

    private XmpCollector xmpCollector;

    @Before
    public void setUp() {
        xmpCollector = new XmpCollector();
    }

    @Test
    public void canDetermineAppropriateResourceType() throws CollectionException {
        NodeLevelResource nodeLevelResource = new NodeLevelResource(1);

        // Define the resource type
        ResourceType resourceType = new ResourceType();
        resourceType.setName("rt");
        resourceType.setLabel("rt label");
        resourceType.setResourceLabel("${instance}");
        StorageStrategy storageStrategy = new StorageStrategy();
        storageStrategy.setClazz(IndexStorageStrategy.class.getCanonicalName());
        resourceType.setStorageStrategy(storageStrategy);
        PersistenceSelectorStrategy persistenceSelectorStrategy = new PersistenceSelectorStrategy();
        persistenceSelectorStrategy.setClazz(PersistAllSelectorStrategy.class.getCanonicalName());
        resourceType.setPersistenceSelectorStrategy(persistenceSelectorStrategy);

        ResourceTypesDao resourceTypesDao = mock(ResourceTypesDao.class);
        when(resourceTypesDao.getResourceTypeByName(resourceType.getName())).thenReturn(resourceType);
        xmpCollector.setResourceTypesDao(resourceTypesDao);

        // If the nodeTypeName is set to "node" it should always return a node level resource
        assertThat(xmpCollector.getResource(nodeLevelResource, CollectionResource.RESOURCE_TYPE_NODE, null, "instance"), instanceOf(NodeLevelResource.class));
        assertThat(xmpCollector.getResource(nodeLevelResource, CollectionResource.RESOURCE_TYPE_NODE, "some-resource", "instance"), instanceOf(NodeLevelResource.class));
        // If a resource-type is set, it should always return a generic type resource
        assertThat(xmpCollector.getResource(nodeLevelResource, null, "rt", "instance"), instanceOf(GenericTypeResource.class));
        // Otherwise, falls back to an instance level resource
        assertThat(xmpCollector.getResource(nodeLevelResource, null, null, "instance"), instanceOf(InterfaceLevelResource.class));
    }

}

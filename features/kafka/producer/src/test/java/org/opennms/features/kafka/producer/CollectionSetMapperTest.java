/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.features.kafka.producer.collection.CollectionSetMapper;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;

public class CollectionSetMapperTest {


    @Test
    public void testCollectionSetForInterfaceResource() throws UnknownHostException {

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));

        CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        NodeLevelResource nodeResource = new NodeLevelResource(1);
        // Null instance should not build any collection set.
        InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeResource, null);

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(0));

        // If Instance is Integer, it is mostly IfIndex.
        interfaceLevelResource = new InterfaceLevelResource(nodeResource, "25");
        collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasInterface());
        assertThat(collectionSetResource.getInterface().getIfIndex(), Matchers.is(25));
        assertThat(collectionSetResource.getInterface().getInstance(), Matchers.is("25"));
    }
}

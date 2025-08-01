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
package org.opennms.features.kafka.producer;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
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
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;

public class CollectionSetMapperTest {


    @Test
    public void testCollectionSetForInterfaceResource() throws UnknownHostException {

        ServiceParameters EMPTY_PARAMS = new ServiceParameters(Collections.emptyMap());

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));

        CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        NodeLevelResource nodeResource = new NodeLevelResource(1);
        // Null instance should not build any collection set.
        InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeResource, null);

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(0));

        // If Instance is Integer, it is mostly IfIndex.
        interfaceLevelResource = new InterfaceLevelResource(nodeResource, "25");
        collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasInterface());
        assertThat(collectionSetResource.getInterface().getIfIndex(), Matchers.is(25));
        assertThat(collectionSetResource.getInterface().getInstance(), Matchers.is("25"));
    }
}

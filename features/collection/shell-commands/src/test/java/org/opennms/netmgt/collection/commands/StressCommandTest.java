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
package org.opennms.netmgt.collection.commands;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class StressCommandTest {
    @Captor
    private ArgumentCaptor<AttributeGroup> attributeGroupArgumentCaptor;

    @Test
    public void testExtraLength() {
        StressCommand command = Mockito.spy(new StressCommand() {
            {
                numberOfNodes = 1;
                numberOfInterfacesPerNode = 1;
                numberOfNumericAttributesPerGroup = 1;
                numberOfStringAttributesPerGroup = 1;
                numberOfGroupsPerInterface = 1;
                metricExtraLength = 20;
                metricExtraLengthVariance = 10;
                resourceExtraLength = 20;
                resourceExtraLengthVariance = 10;
            }
        });

        int nodeId = 1;
        int interfaceId = 1;
        CollectionAgent agent = new StressCommand.MockCollectionAgent(nodeId);
        NodeLevelResource nodeResource = new NodeLevelResource(nodeId);
        InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeResource, "tap" + interfaceId);

        CollectionSet collectionSet = command.generateCollectionSet(agent, nodeId, interfaceId, interfaceResource);
        CollectionSetVisitor visitor = Mockito.mock(CollectionSetVisitor.class);
        collectionSet.visit(visitor);

        Mockito.verify(visitor).visitGroup(attributeGroupArgumentCaptor.capture());
        List<AttributeGroup> groups = attributeGroupArgumentCaptor.getAllValues();
        Assert.assertEquals(1, groups.size());
        AttributeGroup group = groups.get(0);
        Assert.assertEquals("group0_f4fd7231ec2", group.getName());
        Assert.assertEquals("metric_0_0b41af1fa521", group.getAttributes().stream().findFirst().get().getName());
    }
}

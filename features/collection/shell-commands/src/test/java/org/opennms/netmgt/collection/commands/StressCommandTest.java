/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

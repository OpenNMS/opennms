/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.apilayer.collectors.CollectionSetMapper;
import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.resource.AttributeBuilder;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetBuilder;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResourceBuilder;
import org.opennms.integration.api.v1.collectors.resource.GenericTypeResource;
import org.opennms.integration.api.v1.collectors.resource.IpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.NodeResource;
import org.opennms.integration.api.v1.collectors.resource.NumericAttribute;
import org.opennms.integration.api.v1.collectors.resource.Resource;
import org.opennms.integration.api.v1.collectors.resource.ResourceBuilder;
import org.opennms.integration.api.v1.collectors.resource.StringAttribute;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.model.beans.NodeBean;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.model.ResourcePath;

public class CollectionSetMapperTest {

    private static final String GENERIC_INSTANCE = "sample-instance";
    private static final String NODE_LABEL = "piedmont";
    private static final String IP_INSTANCE = "opennms";
    private static final String RESOURCE_NAME = "sample-resource";
    private static final double GAUGE_VALUE = 6.32;
    private static final double COUNTER_VALUE = 45.0;
    private static final String STRING_VALUE = "collection";

    private NodeBean node;

    @Before
    public void setUp() {
        node = new NodeBean();
        node.setId(36);
        node.setForeignSource("fs");
        node.setForeignId("fid");
        node.setLabel(NODE_LABEL);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCollectionSetMappingFromIntegrationAPI() {

        // Mock NodeDao.
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.getNodeByCriteria(anyString())).thenReturn(node);
        // Mock the ResourceType and other storage strategies.
        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn(RESOURCE_NAME);
        when(rt.getStorageStrategy().getClazz()).thenReturn(IndexStorageStrategy.class.getCanonicalName());
        when(rt.getStorageStrategy().getParameters()).thenReturn(Collections.emptyList());
        when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(PersistAllSelectorStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getParameters()).thenReturn(Collections.emptyList());
        ResourceTypeMapper.getInstance().setResourceTypeMapper(type -> rt);

        // Create a CollectionSet From Integration API.
        CollectionSet collectionSet = createCollectionSetFromIntegrationAPI();
        org.opennms.netmgt.collection.support.builder.CollectionSetBuilder builder =
                new org.opennms.netmgt.collection.support.builder.CollectionSetBuilder(new CollectionAgentImpl());
        // Map CollectionSet from Integration API to  default CollectionSet
        org.opennms.netmgt.collection.api.CollectionSet collectionSet1 = CollectionSetMapper.buildCollectionSet(builder, collectionSet);

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao);
        CollectionSet collectionSetResult = collectionSetMapper.buildCollectionSet(new CollectionSetBuilder(), collectionSet1);

        assertThat(collectionSetResult.getCollectionSetResources().size(), is(3));
        for (CollectionSetResource collectionSetResource : collectionSetResult.getCollectionSetResources()) {
            Resource resource = collectionSetResource.getResource();
            if (resource instanceof GenericTypeResource) {
                GenericTypeResource genericTypeResource = (GenericTypeResource) resource;
                assertTrue(genericTypeResource.getInstance().equals(GENERIC_INSTANCE));
                assertTrue(genericTypeResource.getNodeResource().getNodeLabel().equals(NODE_LABEL));
                List<NumericAttribute> numericAttributes = collectionSetResource.getNumericAttributes();
                assertThat(numericAttributes.size(), is(1));
                assertThat(numericAttributes.get(0).getValue(), is(COUNTER_VALUE));
                assertTrue(numericAttributes.get(0).getType().equals(NumericAttribute.Type.COUNTER));
                List<StringAttribute> stringAttributes = collectionSetResource.getStringAttributes();
                assertThat(stringAttributes.size(), is(1));
                assertThat(stringAttributes.get(0).getName(), is(STRING_VALUE));
            } else if (resource instanceof IpInterfaceResource) {
                IpInterfaceResource ipInterfaceResource = (IpInterfaceResource) resource;
                assertTrue(ipInterfaceResource.getInstance().equals(IP_INSTANCE));
                List<NumericAttribute> numericAttributes = collectionSetResource.getNumericAttributes();
                assertThat(numericAttributes.size(), is(1));
                assertThat(numericAttributes.get(0).getValue(), is(GAUGE_VALUE));
                assertTrue(numericAttributes.get(0).getType().equals(NumericAttribute.Type.GAUGE));
            }
        }

    }

    @SuppressWarnings("unchecked")
    private CollectionSet createCollectionSetFromIntegrationAPI() {
        // Every resource needs a node resource.
        NodeResource nodeResource = new ResourceBuilder()
                .withNodeId(node.getId())
                .withNodeLabel(node.getLabel())
                .withForeignId(node.getForeignId())
                .withForeignSource(node.getForeignSource())
                .buildNodeResource();
        IpInterfaceResource ipInterfaceResource = new ResourceBuilder()
                .withInstance(IP_INSTANCE)
                .buildIpInterfaceResource(nodeResource);
        GenericTypeResource genericTypeResource = new ResourceBuilder()
                .withType(RESOURCE_NAME)
                .withInstance(GENERIC_INSTANCE)
                .buildGenericTypeResource(nodeResource);

        NumericAttribute numericAttribute1 = new AttributeBuilder()
                .withName("snmp")
                .withGroup("group")
                .withType(NumericAttribute.Type.GAUGE)
                .withNumericValue(5.89)
                .buildNumeric();
        NumericAttribute numericAttribute2 = new AttributeBuilder()
                .withName("jmx")
                .withGroup("group2")
                .withType(NumericAttribute.Type.GAUGE)
                .withNumericValue(GAUGE_VALUE)
                .buildNumeric();
        NumericAttribute numericAttribute3 = new AttributeBuilder()
                .withName("jdbc")
                .withGroup("group3")
                .withType(NumericAttribute.Type.COUNTER)
                .withNumericValue(COUNTER_VALUE)
                .buildNumeric();

        StringAttribute stringAttribute = new AttributeBuilder()
                .withName(STRING_VALUE)
                .withGroup("group4")
                .withStringValue("kafka")
                .buildString();

        CollectionSetResource<NodeResource> nodeCollectionSet = new CollectionSetResourceBuilder<NodeResource>()
                .withResource(nodeResource)
                .withNumericAttribute(numericAttribute1)
                .build();
        CollectionSetResource<IpInterfaceResource> ipInterfaceCollectionSet = new CollectionSetResourceBuilder<IpInterfaceResource>()
                .withResource(ipInterfaceResource)
                .withNumericAttribute(numericAttribute2)
                .build();
        CollectionSetResource<GenericTypeResource> genericTypeCollectionSet = new CollectionSetResourceBuilder<GenericTypeResource>()
                .withResource(genericTypeResource)
                .withNumericAttribute(numericAttribute3)
                .withStringAttribute(stringAttribute)
                .build();

        CollectionSet collectionSet = new CollectionSetBuilder().withCollectionSetResource(nodeCollectionSet)
                .withCollectionSetResource(ipInterfaceCollectionSet)
                .withCollectionSetResource(genericTypeCollectionSet)
                .withTimeStamp(System.currentTimeMillis())
                .withStatus(CollectionSet.Status.SUCCEEDED)
                .build();
        return collectionSet;
    }

    public static class CollectionAgentImpl implements CollectionAgent {

        @Override
        public InetAddress getAddress() {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                //pass
            }
            return null;
        }

        @Override
        public Set<String> getAttributeNames() {
            return new HashSet<>();
        }

        @Override
        public <V> V getAttribute(String property) {
            return null;
        }

        @Override
        public Object setAttribute(String property, Object value) {
            return null;
        }

        @Override
        public Boolean isStoreByForeignSource() {
            return null;
        }

        @Override
        public String getHostAddress() {
            return null;
        }

        @Override
        public int getNodeId() {
            return 0;
        }

        @Override
        public String getNodeLabel() {
            return null;
        }

        @Override
        public String getForeignSource() {
            return null;
        }

        @Override
        public String getForeignId() {
            return null;
        }

        @Override
        public String getLocationName() {
            return null;
        }

        @Override
        public ResourcePath getStorageResourcePath() {
            return null;
        }

        @Override
        public long getSavedSysUpTime() {
            return 0;
        }

        @Override
        public void setSavedSysUpTime(long sysUpTime) {

        }
    }
}

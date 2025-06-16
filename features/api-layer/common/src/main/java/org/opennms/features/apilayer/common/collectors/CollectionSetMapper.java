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
package org.opennms.features.apilayer.common.collectors;

import java.util.Date;
import java.util.List;

import org.opennms.integration.api.v1.collectors.CollectionSet;
import org.opennms.integration.api.v1.collectors.immutables.ImmutableNumericAttribute;
import org.opennms.integration.api.v1.collectors.immutables.ImmutableStringAttribute;
import org.opennms.integration.api.v1.collectors.resource.CollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.GenericTypeResource;
import org.opennms.integration.api.v1.collectors.resource.IpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.NodeResource;
import org.opennms.integration.api.v1.collectors.resource.NumericAttribute;
import org.opennms.integration.api.v1.collectors.resource.Resource;
import org.opennms.integration.api.v1.collectors.resource.StringAttribute;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSet;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableCollectionSetResource;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableGenericTypeResource;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableIpInterfaceResource;
import org.opennms.integration.api.v1.collectors.resource.immutables.ImmutableNodeResource;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.model.ResourceTypeUtils;

import com.google.common.base.Enums;

/**
 * Helper class that maps @{@link CollectionSet} from Integration API to @{@link org.opennms.netmgt.collection.api.CollectionSet} and vice versa.
 */
public class CollectionSetMapper {

    private NodeDao nodeDao;

    public CollectionSetMapper(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    /**
     *  Maps @{@link org.opennms.netmgt.collection.api.CollectionSet} to Integration API @{@link CollectionSet} and builds it.
     */
    @SuppressWarnings("unchecked")
    public CollectionSet buildCollectionSet(ImmutableCollectionSet.Builder builder, org.opennms.netmgt.collection.api.CollectionSet collectionSet) {

        collectionSet.visit(new CollectionSetVisitor() {
            ImmutableCollectionSetResource.Builder resourceBuilder;
            String groupName = null;

            @Override
            public void visitCollectionSet(org.opennms.netmgt.collection.api.CollectionSet set) {

            }

            @Override
            public void visitResource(CollectionResource resource) {
                if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_NODE)) {
                    resourceBuilder = ImmutableCollectionSetResource.newBuilder(NodeResource.class);
                    resourceBuilder.setResource(buildNodeResource(resource));
                } else if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_IF)) {
                    resourceBuilder = ImmutableCollectionSetResource.newBuilder(IpInterfaceResource.class);
                    IpInterfaceResource ipInterfaceResource = ImmutableIpInterfaceResource.newInstance(buildNodeResource(resource), resource.getInstance());
                    resourceBuilder.setResource(ipInterfaceResource);
                } else {
                    resourceBuilder = ImmutableCollectionSetResource.newBuilder(GenericTypeResource.class);
                    GenericTypeResource genericTypeResource = ImmutableGenericTypeResource.newBuilder()
                            .setInstance(resource.getInstance())
                            .setType(resource.getResourceTypeName())
                            .setNodeResource(buildNodeResource(resource))
                            .build();
                    resourceBuilder.setResource(genericTypeResource);
                }
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                groupName = group.getName();
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    StringAttribute stringAttribute = ImmutableStringAttribute.newBuilder()
                            .setName(attribute.getName())
                            .setValue(attribute.getStringValue())
                            .build();
                    resourceBuilder.addStringAttribute(stringAttribute);
                } else {
                    NumericAttribute numericAttribute = ImmutableNumericAttribute.newBuilder()
                            .setName(attribute.getName())
                            .setGroup(groupName)
                            .setType((attribute.getType() == AttributeType.COUNTER) ? NumericAttribute.Type.COUNTER : NumericAttribute.Type.GAUGE)
                            .setValue(attribute.getNumericValue().doubleValue())
                            .build();
                    resourceBuilder.addNumericAttribute(numericAttribute);
                }
            }

            @Override
            public void completeAttribute(CollectionAttribute attribute) {

            }

            @Override
            public void completeGroup(AttributeGroup group) {

            }

            @Override
            public void completeResource(CollectionResource resource) {
                builder.addCollectionSetResource(resourceBuilder.build());
            }

            @Override
            public void completeCollectionSet(org.opennms.netmgt.collection.api.CollectionSet set) {
                builder.setTimestamp(set.getCollectionTimestamp().getTime());
                builder.setStatus(CollectionSet.Status.SUCCEEDED);
            }
        });
        return builder.build();
    }

    private NodeResource buildNodeResource(CollectionResource collectionResource) {
        String nodeCriteria = getNodeCriteriaFromResource(collectionResource);
        Node node = nodeDao.getNodeByCriteria(nodeCriteria);
        NodeResource nodeResource = ImmutableNodeResource.newBuilder()
                .setNodeId(node.getId())
                .setForeignId(node.getForeignId())
                .setForeignSource(node.getForeignSource())
                .setNodeLabel(node.getLabel())
                .build();
        return nodeResource;
    }

    private String getNodeCriteriaFromResource(CollectionResource resource) {
        String nodeCriteria = null;
        if (resource.getParent() != null) {
            String[] resourcePathArray = resource.getParent().elements();
            if (resourcePathArray != null && resourcePathArray.length > 0) {
                if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(resourcePathArray[0])
                        && resourcePathArray.length == 3) {
                    // parent denotes nodeCriteria, form fs:fid
                    nodeCriteria = resourcePathArray[1] + ":" + resourcePathArray[2];
                } else if (checkNumeric(resourcePathArray[0])) {
                    // parent denotes nodeId
                    nodeCriteria = resourcePathArray[0];
                }
            }
        }
        return nodeCriteria;
    }

    private boolean checkNumeric(String nodeCriteria) {
        try {
            Integer.parseInt(nodeCriteria);
            return true;
        } catch (NumberFormatException e) {
            // not a number
            return false;
        }
    }

    /**
     * Maps @{@link CollectionSet} from Integration API to @{@link org.opennms.netmgt.collection.api.CollectionSet} and builds it.
     */
    public static org.opennms.netmgt.collection.api.CollectionSet buildCollectionSet(org.opennms.netmgt.collection.support.builder.CollectionSetBuilder builder, CollectionSet collectionSet) {
        for (CollectionSetResource collectionSetResource : collectionSet.getCollectionSetResources()) {
            Resource resource = collectionSetResource.getResource();
            if (resource == null) {
                continue;
            }
            if (resource.getResourceType().equals(Resource.Type.NODE)) {
                NodeResource nodeResource = (NodeResource) resource;
                NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeResource.getNodeId());
                addAttributes(collectionSetResource, builder, nodeLevelResource);
            } else if (resource.getResourceType().equals(Resource.Type.INTERFACE)) {
                IpInterfaceResource ipResource = (IpInterfaceResource) resource;
                NodeLevelResource nodeLevelResource = new NodeLevelResource(ipResource.getNodeResource().getNodeId());
                InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeLevelResource, ipResource.getInstance());
                addAttributes(collectionSetResource, builder, interfaceLevelResource);
            } else if (resource.getResourceType().equals(Resource.Type.GENERIC)) {
                GenericTypeResource genericTypeResource = (GenericTypeResource) resource;
                NodeLevelResource nodeLevelResource = new NodeLevelResource(genericTypeResource.getNodeResource().getNodeId());
                DeferredGenericTypeResource deferredGenericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, genericTypeResource.getType(), genericTypeResource.getInstance());
                addAttributes(collectionSetResource, builder, deferredGenericTypeResource);
            }
        }
        builder.withTimestamp(new Date(collectionSet.getTimeStamp()));
        return builder.build();
    }

    @SuppressWarnings({ "unchecked" })
    public static void  addAttributes(CollectionSetResource collectionSetResource, org.opennms.netmgt.collection.support.builder.CollectionSetBuilder builder, org.opennms.netmgt.collection.support.builder.Resource resource) {
        List<NumericAttribute> numericAttributes = collectionSetResource.getNumericAttributes();
        for (NumericAttribute numericAttribute : numericAttributes) {
            AttributeType attributeType = Enums.getIfPresent(AttributeType.class, numericAttribute.getType().name()).or(AttributeType.GAUGE);
            builder.withNumericAttribute(resource, numericAttribute.getGroup(), numericAttribute.getName(), numericAttribute.getValue(), attributeType);
        }
        List<StringAttribute> stringAttributes = collectionSetResource.getStringAttributes();
        for (StringAttribute stringAttribute : stringAttributes) {
            builder.withStringAttribute(resource, stringAttribute.getGroup(), stringAttribute.getName(), stringAttribute.getValue());
        }
    }
}

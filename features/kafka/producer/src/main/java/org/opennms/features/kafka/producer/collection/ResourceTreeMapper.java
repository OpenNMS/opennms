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
package org.opennms.features.kafka.producer.collection;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.opennms.core.utils.LocationUtils;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;

import com.google.common.base.Strings;

/**
 * Synthesizes {@link CollectionSetProtos.CollectionSetResource} messages from the persisted resource tree
 * (as exposed by the {@link org.opennms.netmgt.dao.api.ResourceDao}), mimicking what the
 * {@link CollectionSetMapper} would have produced for the same resource at collection time.
 *
 * This allows the metric filter to be evaluated against the resources that have been collected
 * for a node without triggering an actual collection. Attribute values are not available from the
 * tree, so attributes are emitted without values: only their names (and string values) are meaningful.
 *
 * The instance of a generic resource is the resource tree name, i.e. the storage label. The producer
 * sends the raw index instead, which only matches the label for the default index storage strategy;
 * callers can detect the other strategies through the ResourceTypesDao.
 *
 * Resources the producer would not forward are not mapped at all (the mapper returns null): resources
 * that do not belong to a node, perspective response time resources (which carry no node criteria at
 * collection time) and service status resources (whose collection resource has no parent path).
 * {@link #skipReason(OnmsResource, OnmsNode)} explains why a resource is not mapped.
 *
 * The resource tree lists every sub-directory of a node that is not a known resource type as an
 * interfaceSnmp resource. Those without a matching SNMP interface are the directories non-SNMP collectors
 * (JMX, XML, WMI, ...) use for their node-level data, which the producer forwards as a node-level
 * resource: they are mapped to nodeSnmp, see {@link #isNodeLevelDirectory(OnmsResource)}, and callers
 * should merge them with the node's own nodeSnmp resource.
 */
public class ResourceTreeMapper {

    /** Resource type name of node level resources in the resource tree. */
    public static final String NODE_RESOURCE_TYPE_NAME = "nodeSnmp";
    /** Resource type name of interface level resources in the resource tree. */
    public static final String INTERFACE_RESOURCE_TYPE_NAME = "interfaceSnmp";
    /** Resource type name of response time resources in the resource tree. */
    public static final String RESPONSE_TIME_RESOURCE_TYPE_NAME = "responseTime";
    /** Resource type name of perspective response time resources in the resource tree. */
    public static final String PERSPECTIVE_RESPONSE_TIME_RESOURCE_TYPE_NAME = "perspectiveResponseTime";
    /** Resource type name of service status resources in the resource tree. */
    public static final String STATUS_RESOURCE_TYPE_NAME = "status";

    /** Parent resource type used in resource ids when storing by foreign source, see DefaultResourceDao. */
    static final String NODE_SOURCE_PARENT_TYPE = "nodeSource";
    /** Parent resource type used in resource ids when storing by node id, see DefaultResourceDao. */
    static final String NODE_PARENT_TYPE = "node";

    /**
     * Maps the given resource, locating its node by walking up the parent chain.
     *
     * @return the protobuf resource, or null if the producer would not forward this resource
     */
    public CollectionSetProtos.CollectionSetResource toProto(final OnmsResource resource) {
        return toProto(resource, findNode(resource));
    }

    /**
     * Maps the given resource that belongs to the given node.
     *
     * @param node the node the resource belongs to, may be null
     * @return the protobuf resource, or null if the producer would not forward this resource
     */
    public CollectionSetProtos.CollectionSetResource toProto(final OnmsResource resource, final OnmsNode node) {
        if (skipReason(resource, node) != null) {
            return null;
        }
        // Node-level directories of other collectors are forwarded as node-level resources
        final String type = isNodeLevelDirectory(resource) ? NODE_RESOURCE_TYPE_NAME : resource.getResourceType().getName();

        final CollectionSetProtos.NodeLevelResource.Builder nodeLevel = buildNodeLevelResource(node);
        final CollectionSetProtos.CollectionSetResource.Builder builder = CollectionSetProtos.CollectionSetResource.newBuilder();

        if (RESPONSE_TIME_RESOURCE_TYPE_NAME.equals(type)) {
            // Response time resources carry node information (from the node_id tag) but no resource id
            // fields, and only name their (sanitized) location when it is not the default one.
            final CollectionSetProtos.ResponseTimeResource.Builder response = CollectionSetProtos.ResponseTimeResource.newBuilder()
                    .setInstance(resource.getName())
                    .setNode(nodeLevel);
            final String location = locationOf(node);
            if (location != null && !LocationUtils.isDefaultLocationName(location)) {
                response.setLocation(ResourcePath.sanitize(location));
            }
            builder.setResponse(response);
            addAttributes(builder, resource);
            return builder.build();
        }

        final String resourceLabel;
        if (NODE_RESOURCE_TYPE_NAME.equals(type)) {
            builder.setNode(nodeLevel);
            resourceLabel = "";
        } else if (INTERFACE_RESOURCE_TYPE_NAME.equals(type)) {
            builder.setInterface(CollectionSetProtos.InterfaceLevelResource.newBuilder()
                    .setNode(nodeLevel)
                    .setInstance(resource.getName())
                    .setIfIndex(((OnmsSnmpInterface) resource.getEntity()).getIfIndex()));
            resourceLabel = resource.getName();
        } else {
            builder.setGeneric(CollectionSetProtos.GenericTypeResource.newBuilder()
                    .setNode(nodeLevel)
                    .setType(type)
                    .setInstance(resource.getName()));
            resourceLabel = resource.getName();
        }

        // Build the resource id the way DefaultResourceDao.getResourceId does at collection time. The tree
        // names node resources "fs:fid" whenever both are set, which the producer never does.
        final ResourceId resourceId = parentResourceId(node).resolve(type, resourceLabel);
        setIfNotEmpty(resourceId.toString(), builder::setResourceId);
        setIfNotEmpty(resourceId.getName(), builder::setResourceName);
        setIfNotEmpty(resourceId.getType(), builder::setResourceTypeName);
        addAttributes(builder, resource);
        return builder.build();
    }

    /**
     * Explains why the producer would not forward the given tree resource.
     *
     * @return a human readable reason, or null when the resource is forwarded (i.e. can be mapped)
     */
    public String skipReason(final OnmsResource resource, final OnmsNode node) {
        Objects.requireNonNull(resource);
        if (node == null || node.getId() == null) {
            // Without node criteria the CollectionSetMapper never emits the resource
            return "not associated with a node";
        }
        final String type = resource.getResourceType().getName();
        if (PERSPECTIVE_RESPONSE_TIME_RESOURCE_TYPE_NAME.equals(type)) {
            // Collected as interface resources without node criteria: never forwarded
            return "perspective response times are never forwarded";
        }
        if (STATUS_RESOURCE_TYPE_NAME.equals(type)) {
            // Collected as latency resources without a parent path: never forwarded
            return "service status is never forwarded";
        }
        return null;
    }

    /**
     * Whether the given tree resource is a sub-directory of the node that holds node-level data of a
     * non-SNMP collector: listed as interfaceSnmp by the tree, but without a matching SNMP interface.
     */
    public boolean isNodeLevelDirectory(final OnmsResource resource) {
        if (!INTERFACE_RESOURCE_TYPE_NAME.equals(resource.getResourceType().getName())) {
            return false;
        }
        final OnmsEntity entity = resource.getEntity();
        return !(entity instanceof OnmsSnmpInterface) || ((OnmsSnmpInterface) entity).getIfIndex() == null;
    }

    static ResourceId parentResourceId(OnmsNode node) {
        if (ResourceTypeUtils.isStoreByForeignSource() && node.getForeignSource() != null && node.getForeignId() != null) {
            return ResourceId.get(NODE_SOURCE_PARENT_TYPE, node.getForeignSource() + ":" + node.getForeignId());
        }
        return ResourceId.get(NODE_PARENT_TYPE, String.valueOf(node.getId()));
    }

    public static OnmsNode findNode(OnmsResource resource) {
        for (OnmsResource r = resource; r != null; r = r.getParent()) {
            if (r.getEntity() instanceof OnmsNode) {
                return (OnmsNode) r.getEntity();
            }
        }
        return null;
    }

    private static String locationOf(OnmsNode node) {
        return node.getLocation() != null ? node.getLocation().getLocationName() : null;
    }

    private static CollectionSetProtos.NodeLevelResource.Builder buildNodeLevelResource(OnmsNode node) {
        final CollectionSetProtos.NodeLevelResource.Builder builder = CollectionSetProtos.NodeLevelResource.newBuilder()
                .setNodeId(node.getId());
        setIfNotEmpty(node.getLabel(), builder::setNodeLabel);
        setIfNotEmpty(node.getForeignSource(), builder::setForeignSource);
        setIfNotEmpty(node.getForeignId(), builder::setForeignId);
        setIfNotEmpty(locationOf(node), builder::setLocation);
        return builder;
    }

    private static void addAttributes(CollectionSetProtos.CollectionSetResource.Builder builder, OnmsResource resource) {
        resource.getRrdGraphAttributes().keySet().stream().sorted().forEach(name ->
                builder.addNumeric(CollectionSetProtos.NumericAttribute.newBuilder()
                        .setName(name)));
        resource.getStringPropertyAttributes().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> builder.addString(CollectionSetProtos.StringAttribute.newBuilder()
                        .setName(e.getKey())
                        .setValue(Strings.nullToEmpty(e.getValue()))));
    }

    private static void setIfNotEmpty(String value, Consumer<String> setter) {
        if (!Strings.isNullOrEmpty(value)) {
            setter.accept(value);
        }
    }
}

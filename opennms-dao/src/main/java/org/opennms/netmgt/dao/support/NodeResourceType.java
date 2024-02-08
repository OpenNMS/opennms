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
package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.collections.LazyList;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Nodes are top-level resources stored in paths like:
 *   snmp/${nodeId}/ds.rrd
 * or when storeByFs is enabled:
 *   snmp/fs/${fs}/${fid}/ds.rrd
 *
 * Note that the node resource may exist even if it's path
 * is empty since we consider response time resources, which
 * are stored in other folders, to be children.
 *
 */
public final class NodeResourceType extends AbstractTopLevelResourceType {

    public static final String RESOURCE_TYPE_NAME = "node";

    /** Constant <code>s_emptyAttributeSet</code> */
    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());

    private final ResourceDao m_resourceDao;
    private final NodeDao m_nodeDao;

    /**
     * <p>Constructor for NodeResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public NodeResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
        m_resourceDao = resourceDao;
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Node";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "node";
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return "element/node.jsp?node=" + resource.getName();
    }

    @Override
    public List<OnmsResource> getTopLevelResources() {
        return m_nodeDao.findAll().stream()
                // Only return non-deleted nodes - see NMS-2977
                .filter(node -> node.getType() == null || !node.getType().equals(NodeType.DELETED))
                .map(this::createResourceForNode)
                .collect(Collectors.toList());
    }

    @Override
    public OnmsResource getResourceByName(String nodeLookupCriteria) {
        // The nodeDao automatically handles both id type, and fs:fid type lookups
        final OnmsNode node = m_nodeDao.get(nodeLookupCriteria);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, nodeLookupCriteria, "Top-level resource of resource type node could not be found: " + nodeLookupCriteria, null);
        }

        // We don't check the existence of the resource path, since the
        // resource may exists, even if this directory is empty
        // i.e. there are response[] type resources but no nodeSnmp[] resources

        return createResourceForNode(node);
    }

    protected static ResourcePath getResourcePathForNode(OnmsNode node) {
        // Use the storeByFs path when enabled, falling back to the node id path when
        // the node in question has no foreign source or foreign id
        if (ResourceTypeUtils.isStoreByForeignSource() && node.getForeignSource() != null && node.getForeignId() != null) {
            return ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY, ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY, node.getForeignSource(), node.getForeignId());
        } else {
            return ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY, Integer.toString(node.getId()));
        }
    }

    protected OnmsResource createResourceForNode(OnmsNode node) {
        final ResourcePath path = getResourcePathForNode(node);
        final LazyChildResourceLoader loader = new LazyChildResourceLoader(m_resourceDao);     
        final String resourceName = node.getForeignSource() != null && node.getForeignId() != null ?
                String.format("%s:%s", node.getForeignSource(), node.getForeignId()) : Integer.toString(node.getId());
        final OnmsResource resource = new OnmsResource(resourceName, node.getLabel(),
                this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader), path);
        resource.setEntity(node);
        loader.setParent(resource);
        return resource;
    }

    /**
     * Convenience method.
     */
    public static boolean isNode(OnmsResource resource) {
        if (resource == null) {
            return false;
        }
        return resource.getResourceType() instanceof NodeResourceType;
    }
}

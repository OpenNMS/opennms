/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.LazyList;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Node sources are top-level resources stored in paths like:
 *   snmp/fs/${fs}/${fid}/ds.rrd
 *
 * Note that the node source resource may exist even if it's path
 * is empty since we consider response time resources, which
 * are stored in other folders, to be children.
 *
 */
public final class NodeSourceResourceType extends AbstractTopLevelResourceType {

    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    private final ResourceDao m_resourceDao;
    private final NodeDao m_nodeDao;

    /**
     * <p>Constructor for NodeSourceResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     * @param nodeDao
     */
    public NodeSourceResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
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
        return "Foreign Source";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "nodeSource";
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return "element/node.jsp?node=" + resource.getName();
    }

    @Override
    public List<OnmsResource> getTopLevelResources() {
        // We handle these in the DefaultResourceDao class
        // instead of here since we may need to create node[] types
        return Collections.emptyList();
    }

    @Override
    public OnmsResource getResourceByName(String nodeSource) {
        final String[] ident = ResourceTypeUtils.getFsAndFidFromNodeSource(nodeSource);

        final OnmsNode node = m_nodeDao.findByForeignId(ident[0], ident[1]);
        if (node == null) {
             throw new ObjectRetrievalFailureException(OnmsNode.class, nodeSource, "Top-level resource of resource type node could not be found: " + nodeSource, null);
        }

        // We don't check the existence of the resource path, since the
        // resource may exists, even if this directory is empty
        // i.e. there are response[] type resources but no nodeSnmp[] resources

        return createResourceForNode(node);
    }

    protected static ResourcePath getResourcePathForNode(OnmsNode node) {
        // Preserve backwards compatibility by allowing nodes to be referenced by FS:FID
        // even when storeByFs is disabled
        if(!ResourceTypeUtils.isStoreByForeignSource()) {
            return NodeResourceType.getResourcePathForNode(node);
        }

        return ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY, ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY, node.getForeignSource(), node.getForeignId());
    }

    public OnmsResource createResourceForNode(final OnmsNode node) {
        final String label = String.format("%s:%s", node.getForeignSource(), node.getForeignId());
        final ResourcePath path = getResourcePathForNode(node);
        final LazyChildResourceLoader loader = new LazyChildResourceLoader(m_resourceDao);
        final OnmsResource resource = new OnmsResource(label, label,
                this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader), path);
        resource.setEntity(node);
        loader.setParent(resource);
        return resource;
    }
}

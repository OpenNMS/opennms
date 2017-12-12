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
import java.util.List;
import java.util.Set;

import org.opennms.core.collections.LazySet;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Node SNMP resources point to the resources stored in the root
 * of the node path i.e.
 *   snmp/${nodeId}/ds1.rrd
 *   snmp/${nodeId}/ds2.rrd
 *
 */
public final class NodeSnmpResourceType implements OnmsResourceType {

    private final ResourceStorageDao m_resourceStorageDao;

    /**
     * <p>Constructor for NodeSnmpResourceType.</p>
     *
     * @param resourceStorageDao a {@link org.opennms.netmgt.dao.api.ResourceStorageDao} object.
     */
    public NodeSnmpResourceType(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "nodeSnmp";
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "SNMP Node Data";
    }

    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        try {
            checkForNodeSnmpResources(parent);
        } catch (ObjectRetrievalFailureException e) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        if (!isResourceTypeOnParent(parent)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(getResourceForNode(parent));
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getChildByName(OnmsResource parent, String name) {
        // Node-level SNMP resources always have a blank name
        if (!"".equals(name)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "Unsupported name '" + name + "' for node SNMP resource type.");
        }

        checkForNodeSnmpResources(parent);

        // Build the resource
        return getResourceForNode(parent);
    }

    private OnmsResource getResourceForNode(OnmsResource node) {
        final LazyResourceAttributeLoader loader = new LazyResourceAttributeLoader(m_resourceStorageDao, node.getPath());
        final Set<OnmsAttribute> attributes = new LazySet<OnmsAttribute>(loader);
        final OnmsResource resource = new OnmsResource("", "Node-level Performance Data", this, attributes, node.getPath());
        resource.setParent(node);
        return resource;
    }

    private void checkForNodeSnmpResources(OnmsResource parent) {
        // Make sure we have a node
        if (!NodeResourceType.isNode(parent)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "Invalid parent type '" + parent +"' for node SNMP resource type.");
        }

        // Make sure we have one or more metrics in the parent path
        if (!m_resourceStorageDao.exists(parent.getPath(), 0)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "No metrics found in parent path '" + parent.getPath() + "'");
        }
    }
}

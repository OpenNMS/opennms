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
    public static final String NODE_RESOURCE_TYPE_NAME = "nodeSnmp";
    public static final String PARENT_RESOURCE_TYPE_FOR_STORE_BY_FOREIGN_SOURCE = "nodeSource";
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
        return NODE_RESOURCE_TYPE_NAME;
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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

/**
 * The Class JMXCollectionResource.
 */
public class JMXCollectionResource extends AbstractCollectionResource {

    /** The resource name. */
    String m_resourceName;

    /** The node id. */
    private int m_nodeId;

    /**
     * Instantiates a new JMX collection resource.
     *
     * @param agent
     *            the agent
     * @param resourceName
     *            the resource name
     */
    JMXCollectionResource(CollectionAgent agent, String resourceName) {
        super(agent);
        m_resourceName = resourceName;
        m_nodeId = agent.getNodeId();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node[" + m_nodeId + ']';
    }

    /**
     * Sets the attribute value.
     *
     * @param type
     *            the type
     * @param value
     *            the value
     */
    public void setAttributeValue(CollectionAttributeType type, String value) {
        JMXCollectionAttribute attr = new JMXCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.netmgt.collectd.AbstractCollectionResource#getResourceDir(org.opennms.netmgt.model.RrdRepository)
     */
    @Override
    public File getResourceDir(RrdRepository repository) {
        return new File(repository.getRrdBaseDir(), getParent() + File.separator + m_resourceName);
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_NODE; //All node resources for JMX; nothing of interface or "indexed resource" type
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.config.collector.CollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return null; // For node type resources, use the default instance
    }
}

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

/**
 * The Class JMXSingleInstanceCollectionResource.
 */
public class JMXSingleInstanceCollectionResource extends JMXCollectionResource {

    /** The node id. */
    private int m_nodeId;

    /**
     * Instantiates a new JMX single instance collection resource.
     *
     * @param agent the agent
     */
    JMXSingleInstanceCollectionResource(CollectionAgent agent) {
        super(agent);
        m_nodeId = agent.getNodeId();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.JMXCollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return null; //For node type resources, use the default instance
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.JMXCollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return "node"; //All node resources for JMX; nothing of interface or "indexed resource" type
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node["+m_nodeId+"].nodeSnmp[]";
    }
}

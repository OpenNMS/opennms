/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;


/**
 * This class encapsulates all of the node-level data required by the SNMP data
 * collector in order to successfully perform data collection for a scheduled
 * primary SNMP interface.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class NodeInfo extends SnmpCollectionResource {

	private SNMPCollectorEntry m_entry;
    private int m_nodeId;
    private CollectionAgent m_agent;

    /**
     * <p>Constructor for NodeInfo.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.NodeResourceType} object.
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public NodeInfo(NodeResourceType def, CollectionAgent agent) {
        super(def);
        m_agent = agent;
        m_nodeId = agent.getNodeId();
    }
    
     /**
      * <p>getType</p>
      *
      * @return a int.
      */
        @Override
     public int getType() {
        return -1;
    }

    /** {@inheritDoc} */
        @Override
    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        return new File(rrdBaseDir, getCollectionAgent().getStorageDir().toString());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "node["+m_nodeId+']';
    }

    /**
     * <p>setEntry</p>
     *
     * @param nodeEntry a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    public void setEntry(SNMPCollectorEntry nodeEntry) {
        m_entry = nodeEntry;
    }
    
    /**
     * <p>getEntry</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     */
    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }

    /** {@inheritDoc} */
        @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }
    
    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getResourceTypeName() {
        return "node"; //This is a nodeInfo; must be a node type resource
    }
    
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getInstance() {
        return null; //For node type resources, use the default instance
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String getLabel() {
        return null;
    }

        @Override
    public String getParent() {
        return m_agent.getStorageDir().toString();
    }

} // end class

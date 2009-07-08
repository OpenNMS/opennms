/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2004, 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.model.RrdRepository;


/**
 * This class encapsulates all of the node-level data required by the SNMP data
 * collector in order to successfully perform data collection for a scheduled
 * primary SNMP interface.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class NodeInfo extends SnmpCollectionResource {

	private SNMPCollectorEntry m_entry;
    private int m_nodeId;

    public NodeInfo(NodeResourceType def, CollectionAgent agent) {
        super(def);
        m_nodeId = agent.getNodeId();
    }
    
     public int getType() {
        return -1;
    }

    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeRepo = new File(rrdBaseDir, String.valueOf(getCollectionAgent().getNodeId()));
        return nodeRepo;
    }

    public String toString() {
        return "node["+m_nodeId+']';
    }

    public void setEntry(SNMPCollectorEntry nodeEntry) {
        m_entry = nodeEntry;
    }
    
    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }
    
    public String getResourceTypeName() {
        return "node"; //This is a nodeInfo; must be a node type resource
    }
    
    
    public String getInstance() {
        return null; //For node type resources, use the default instance
    }

    public String getLabel() {
        return null;
    }

} // end class

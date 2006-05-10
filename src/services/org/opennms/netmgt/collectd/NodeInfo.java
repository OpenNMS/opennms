//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Category;


/**
 * This class encapsulates all of the node-level data required by the SNMP data
 * collector in order to successfully perform data collection for a scheduled
 * primary SNMP interface.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class NodeInfo extends CollectionResource {

	private SNMPCollectorEntry m_entry;

    public NodeInfo(CollectionAgent agent, String collectionName) {
        super(agent, collectionName);
    }
    
     public int getType() {
        return -1;
    }

    File getResourceDir(File rrdBaseDir) {
        File nodeRepo = new File(rrdBaseDir, String.valueOf(getCollectionAgent().getNodeId()));
        return nodeRepo;
    }

    void logUpdateFailed(CollectionAttribute attr) {
        log().warn(
        		"updateRRDs: ds.performUpdate() "
        				+ "failed for node: " + getCollectionAgent().getNodeId()
        				+ " datasource: " + attr.getName());
    }

    public void store(SNMPCollectorEntry nodeEntry, CollectionAttribute attr, File baseDir) {
        if (attr.getDs().performUpdate(getCollectionAgent().getCollection(), getCollectionAgent().getHostAddress(), getResourceDir(baseDir), attr.getDs().getName(), attr.getDs().getRRDValue(nodeEntry))) {
        	logUpdateFailed(attr);
        }
    }

    void logNoDataForValue(DataSource ds1) {
        Category log = log();
        if (log.isDebugEnabled()) {
        	log.debug(
        			"updateRRDs: Skipping update, no "
        					+ "data retrieved for nodeId: "
        					+ getCollectionAgent().getNodeId() + " datasource: "
        					+ ds1.getName());
        }
    }

    void logExceptionOnUpdate(DataSource ds1, IllegalArgumentException e1) {
        Category log = log();
        log.warn("getRRDValue: " + e1.getMessage());
        log.warn(
        		"updateRRDs: call to getRRDValue() failed "
        				+ "for node: " + getCollectionAgent().getNodeId() + " datasource: "
        				+ ds1.getName());
    }

    public void saveAttributeData(File rrdBaseDir) {
        /*
         * Iterate over the node datasource list and issue RRD update
         * commands to update each datasource which has a corresponding
         * value in the collected SNMP data.
         */
        Iterator it = getAttributeList().iterator();
        while (it.hasNext()) {
            CollectionAttribute attr = (CollectionAttribute)it.next();
        	try {
        		if (attr.getDs().getRRDValue(m_entry) == null) {
        			// Do nothing, no update is necessary
        			logNoDataForValue(attr.getDs());
        		} else {
        			
                    store(m_entry, attr, rrdBaseDir);
        		}
        	} catch (IllegalArgumentException e) {
        		logExceptionOnUpdate(attr.getDs(), e);
        	}
      
        } // end while(more datasources)
    }

    public void setEntry(SNMPCollectorEntry nodeEntry) {
        m_entry = nodeEntry;
    }

} // end class

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.SnmpCollector.IfNumberTracker;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.utils.EventProxy;

public class CollectMethod {

    private CollectionAgent m_agent;
    private EventProxy m_eproxy;
    private Map m_parameters;
    private SnmpNodeCollector m_nodeCollector;
    private IfNumberTracker m_ifNumber;
    private SnmpIfCollector m_ifCollector;

    int execute(CollectionAgent agent, EventProxy eproxy, Map parameters) {
        m_agent = agent;
        m_eproxy = eproxy;
        m_parameters = parameters;
        try {
    		doCollection();
    
    		// return the status of the collection
    		return ServiceCollector.COLLECTION_SUCCEEDED;
    	} catch (CollectionError e) {
    		return e.reportError();
    	} catch (Throwable t) {
    		return unexpected(m_agent, t);
    	}
    }

    private void doCollection() throws CollectionError, CollectionWarning {
        // Collect node and interface MIB data from the remote agent
   
        
        // construct the nodeCollector
        createNodeCollector();
   
        createIfNumberTracker();

        createIfCollector();
   
        collectData();
   
        checkForNewInterfaces();
   
        // Update RRD with values retrieved in SNMP collection
        updateRRDs();
    }

    private void checkForNewInterfaces() {
        if (!m_agent.hasInterfaceOids()) return;
        
        logIfCounts();

        if (ifCountHasChanged()) {
            sendForceRescanEvent();
        }

        m_agent.setSavedIfCount(m_ifNumber.getIfNumber());
            
    }

    private void sendForceRescanEvent() {
        if (!m_agent.isForceRescanInProgress()) {
            logIfCountChangedForceRescan();
            m_agent.sendForceRescanEvent(m_eproxy);
        }
    }

    private void createIfCollector() {
        m_ifCollector = null;
        // construct the ifCollector
        if (m_agent.hasInterfaceOids()) {
        	m_ifCollector = new SnmpIfCollector(m_agent.getInetAddress(), m_agent.getCombinedInterfaceAttributes());
        }
    }

    private void createIfNumberTracker() {
        m_ifNumber = null;
        if (m_agent.hasInterfaceOids()) {
            m_ifNumber = new IfNumberTracker();
        }
    }

    private void createNodeCollector() throws CollectionError {
        m_nodeCollector = null;
        if (!m_agent.getNodeAttributeList().isEmpty()) {
        	m_nodeCollector = new SnmpNodeCollector(m_agent.getInetAddress(), m_agent.getNodeAttributeList());
        }
    }

    private void logIfCountChangedForceRescan() {
        log().info("Number of interfaces on primary SNMP "
                + "interface " + m_agent.getHostAddress()
                + " has changed, generating 'ForceRescan' event.");
    }

    private boolean ifCountHasChanged() {
        return (m_agent.getSavedIfCount() != -1) && (m_ifNumber.getIfNumber() != m_agent.getSavedIfCount());
    }

    private void logIfCounts() {
        log().debug("collect: nodeId: " + m_agent.getNodeId()
        				+ " interface: " + m_agent.getHostAddress()
        				+ " ifCount: " + m_ifNumber.getIfNumber() 
                       + " savedIfCount: " + m_agent.getSavedIfCount());
    }

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void collectData() throws CollectionWarning {
    	try {
    
            // now collect the data
    		SnmpWalker walker = createWalker();
    		walker.start();
    
            logStartedWalker();
    
    		// wait for collection to finish
    		walker.waitFor();
    
    		logFinishedWalker();
    
    		// Was the collection successful?
    		verifySuccessfulWalk(walker);
    
    		saveMaxVarsPerPdu(walker);
            
    	} catch (InterruptedException e) {
    		warnOfInterruption(e);
    	}
    }

    private SnmpWalker createWalker() {
        return SnmpUtils.createWalker(getAgentConfig(), "SnmpCollectors for " + m_agent.getInetAddress().getHostAddress(), createCollectionTracker());
    }

    private void warnOfInterruption(InterruptedException e) throws CollectionWarning {
        Thread.currentThread().interrupt();
        throw new CollectionWarning("collect: Collection of node SNMP "
        		+ "data for interface " + m_agent.getHostAddress()
        		+ " interrupted!", e);
    }

    private void saveMaxVarsPerPdu(SnmpWalker walker) {
        m_agent.setMaxVarsPerPdu(walker.getMaxVarsPerPdu());
    }

    private void verifySuccessfulWalk(SnmpWalker walker) throws CollectionWarning {
        if (walker.failed()) {
        	// Log error and return COLLECTION_FAILED
        	throw new CollectionWarning("collect: collection failed for "
        			+ m_agent.getHostAddress());
        }
    }

    private void logFinishedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: node SNMP query for address "
        					+ m_agent.getHostAddress() + " complete.");
        }
    }

    private void logStartedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: successfully instantiated "
        					+ "SnmpNodeCollector() for "
        					+ m_agent.getHostAddress());
        }
    }

    private SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_agent.getInetAddress());
        agentConfig.setMaxVarsPerPdu(m_agent.getMaxVarsPerPdu());
        return agentConfig;
    }

    private CollectionTracker createCollectionTracker() {
        List trackers = new ArrayList(3);
   
        if (m_ifNumber != null) {
        	trackers.add(m_ifNumber);
        }
        if (m_nodeCollector != null) {
        	trackers.add(m_nodeCollector);
        }
        if (m_ifCollector != null) {
        	trackers.add(m_ifCollector);
        }
   
        return new AggregateTracker(trackers);
    }

    /**
     * This method is responsible for building an RRDTool style 'update' command
     * which is issued via the RRD JNI interface in order to push the latest
     * SNMP-collected values into the interface's RRD database.
     * @param collectionName
     *            SNMP data Collection name from 'datacollection-config.xml'
     * @param iface
     *            CollectionInterface object of the interface currently being
     *            polled
     * @param nodeCollector
     *            Node level MIB data collected via SNMP for the polled
     *            interface
     * @param ifCollector
     *            Interface level MIB data collected via SNMP for the polled
     *            interface
     * @param parms TODO
     * @param eproxy TODO
     * 
     * @throws CollectionError
     * @exception RuntimeException
     *                Thrown if the data source list for the interface is null.
     */
    private void updateRRDs() throws CollectionError {
    
        new UpdateRRDs().execute(m_agent, m_nodeCollector, m_ifCollector, m_parameters, m_eproxy);
    }

    private int unexpected(CollectionAgent agent, Throwable t) {
    	log().error(
    			"Unexpected error during node SNMP collection for "
    					+ agent.getHostAddress(), t);
    	return ServiceCollector.COLLECTION_FAILED;
    }

}

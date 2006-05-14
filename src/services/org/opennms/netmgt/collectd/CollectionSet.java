//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;

public class CollectionSet implements Collectable {
	
	public static class RescanNeeded {
        boolean rescanNeeded = false;
        public void rescanIndicated() {
            rescanNeeded = true;
        }
        
        public boolean rescanIsNeeded() {
            return rescanNeeded;
        }
        
    }

    static public final class IfNumberTracker extends SingleInstanceTracker {
    	int m_ifNumber = -1;
    
    	IfNumberTracker() {
    		super(SnmpObjId.get(SnmpCollector.INTERFACES_IFNUMBER), SnmpInstId.INST_ZERO);
    	}
    
    	protected void storeResult(SnmpObjId base, SnmpInstId inst,
    			SnmpValue val) {
    		m_ifNumber = val.toInt();
    	}
    
    	public int getIfNumber() {
    		return m_ifNumber;
    	}
    }

    private CollectionAgent m_agent;
    private NodeResourceType m_nodeResourceType;
    private IfResourceType m_ifResourceType;
    private OnmsSnmpCollection m_snmpCollection;
    private boolean m_rescanTriggered;
    private SnmpIfCollector m_ifCollector;
    private CollectionSet.IfNumberTracker m_ifNumber;
    private SnmpNodeCollector m_nodeCollector;
	
	public CollectionSet(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
		m_agent = agent;
        m_snmpCollection = snmpCollection;
        m_nodeResourceType = new NodeResourceType(m_agent, snmpCollection);
        m_ifResourceType = new IfResourceType(m_agent, snmpCollection);
	}
    
    public SnmpIfCollector getIfCollector() {
        if (m_ifCollector == null)
            m_ifCollector = createIfCollector();
        return m_ifCollector;
    }

    public CollectionSet.IfNumberTracker getIfNumber() {
        if (m_ifNumber == null)
            m_ifNumber = createIfNumberTracker();
        return m_ifNumber;
    }

    public SnmpNodeCollector getNodeCollector() {
        if (m_nodeCollector == null)
            m_nodeCollector = createNodeCollector();
        return m_nodeCollector;
    }

    private SnmpNodeCollector createNodeCollector() {
        SnmpNodeCollector nodeCollector = null;
        if (!getAttributeList().isEmpty()) {
            nodeCollector = new SnmpNodeCollector(m_agent.getInetAddress(), getAttributeList(), this);
        }
        return nodeCollector;
    }

    private CollectionSet.IfNumberTracker createIfNumberTracker() {
        CollectionSet.IfNumberTracker ifNumber = null;
        if (hasInterfaceDataToCollect()) {
            ifNumber = new CollectionSet.IfNumberTracker();
        }
        return ifNumber;
    }

    private SnmpIfCollector createIfCollector() {
        SnmpIfCollector ifCollector = null;
        // construct the ifCollector
        if (hasInterfaceDataToCollect()) {
            ifCollector = new SnmpIfCollector(m_agent.getInetAddress(), getCombinedInterfaceAttributes(), this);
        }
        return ifCollector;
    }
	
	public NodeInfo getNodeInfo() {
        return m_nodeResourceType.getNodeInfo();
	}

	boolean hasDataToCollect() {
        return (m_nodeResourceType.hasDataToCollect() || m_ifResourceType.hasDataToCollect());
	}
    
    boolean hasInterfaceDataToCollect() {
        return m_ifResourceType.hasDataToCollect();
    }

	public CollectionAgent getCollectionAgent() {
		return m_agent;
	}

	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	Collection getAttributeList() {
        return getNodeInfo().getAttributeTypes();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceType#getCombinedInterfaceAttributes()} instead
     */
    List getCombinedInterfaceAttributes() {
        return m_ifResourceType.getCombinedInterfaceAttributes();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceType#getIfInfos()} instead
     */
    public Collection getIfInfos() {
        return m_ifResourceType.getIfInfos();
    }

    public IfInfo getIfInfo(int ifIndex) {
        return m_ifResourceType.getIfInfo(ifIndex);
    }

    public CollectionTracker getCollectionTracker() {
        return new AggregateTracker(getAttributeTypes());
    }

    private Collection getAttributeTypes() {
        return m_snmpCollection.getAttributeTypes(m_agent);
    }

    public Collection getResources() {
        return m_snmpCollection.getResources(m_agent);
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);
        
        for (Iterator iter = getResources().iterator(); iter.hasNext();) {
            CollectionResource resource = (CollectionResource) iter.next();
            resource.visit(visitor);
        }
        
        visitor.completeCollectionSet(this);
    }
    
    public void triggerRescan() {
        m_rescanTriggered = true;
    }

    public boolean rescanTriggered() {
        return m_rescanTriggered;
    }

    CollectionTracker getTracker() {
        List trackers = new ArrayList(3);
       
        if (getIfNumber() != null) {
        	trackers.add(getIfNumber());
        }
        if (getNodeCollector() != null) {
        	trackers.add(getNodeCollector());
        }
        if (getIfCollector() != null) {
        	trackers.add(getIfCollector());
        }
       
        return new AggregateTracker(trackers);
    }

    SnmpWalker createWalker() {
        CollectionAgent agent = getCollectionAgent();
        return SnmpUtils.createWalker(getAgentConfig(), "SnmpCollectors for " + agent.getHostAddress(), getTracker());
    }

    void logStartedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: successfully instantiated "
        					+ "SnmpNodeCollector() for "
        					+ getCollectionAgent().getHostAddress());
        }
    }

    void logFinishedWalker() {
        if (log().isDebugEnabled()) {
            log().debug(
        			"collect: node SNMP query for address "
        					+ getCollectionAgent().getHostAddress() + " complete.");
        }
    }

    void verifySuccessfulWalk(SnmpWalker walker) throws CollectionWarning {
        if (walker.failed()) {
        	// Log error and return COLLECTION_FAILED
        	throw new CollectionWarning("collect: collection failed for "
        			+ getCollectionAgent().getHostAddress());
        }
    }

    void collect() throws CollectionWarning {
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
    
    		getCollectionAgent().setMaxVarsPerPdu(walker.getMaxVarsPerPdu());
            
    	} catch (InterruptedException e) {
    		Thread.currentThread().interrupt();
            throw new CollectionWarning("collect: Collection of node SNMP "
            		+ "data for interface " + getCollectionAgent().getHostAddress()
            		+ " interrupted!", e);
    	}
    }

    void logIfCountChangedForceRescan() {
        log().info("Number of interfaces on primary SNMP "
                + "interface " + getCollectionAgent().getHostAddress()
                + " has changed, generating 'ForceRescan' event.");
    }

    void checkForNewInterfaces(CollectionSet.RescanNeeded rescanNeeded) {
        if (!hasInterfaceDataToCollect()) return;
        
        logIfCounts();
    
        if (ifCountHasChanged(getCollectionAgent())) {
            rescanNeeded.rescanIndicated();
            logIfCountChangedForceRescan();
        }
    
        getCollectionAgent().setSavedIfCount(getIfNumber().getIfNumber());
    }

    private void logIfCounts() {
        CollectionAgent agent = getCollectionAgent();
        log().debug("collect: nodeId: " + agent.getNodeId()
                + " interface: " + agent.getHostAddress()
                + " ifCount: " + getIfNumber().getIfNumber() 
                + " savedIfCount: " + agent.getSavedIfCount());
    }
    
    public boolean rescanNeeded() {
        if (rescanTriggered()) return true;
        
        final RescanNeeded rescanNeeded = new RescanNeeded();
        visit(new ResourceVisitor() {
        
            public void visitResource(CollectionResource resource) {
                if (resource.rescanNeeded())
                    rescanNeeded.rescanIndicated();
            }
            
        });
            
        checkForNewInterfaces(rescanNeeded);
        
        return rescanNeeded.rescanIsNeeded();
    }
    
    public SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig agentConfig = getCollectionAgent().getAgentConfig();
        agentConfig.setMaxVarsPerPdu(computeMaxVarsPerPdu(agentConfig));
        return agentConfig;
    }

    private int computeMaxVarsPerPdu(SnmpAgentConfig agentConfig) {
        int maxVarsPerPdu = getCollectionAgent().getMaxVarsPerPdu();
        if (maxVarsPerPdu < 1) {
            maxVarsPerPdu = m_snmpCollection.getMaxVarsPerPdu();
            log().info("using maxVarsPerPdu from dataCollectionConfig");
        }

        if (maxVarsPerPdu < 1) {
            maxVarsPerPdu = agentConfig.getMaxVarsPerPdu();
            log().info("using maxVarsPerPdu from snmpconfig");
        }

        if (maxVarsPerPdu < 1) {
            log().warn("maxVarsPerPdu CANNOT BE LESS THAN 1.  Using 10");
            return 10;
        }
        return maxVarsPerPdu;
    }

    public void notifyIfNotFound(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        triggerRescan();
        log().info("Unable to locate resource with instance id "+inst+" while collecting attribute "+this);
    }

    void saveAttributes(final ServiceParameters params) {
        OneToOnePersister persister = createPersister(params);
        visit(persister);
    }

    private OneToOnePersister createPersister(ServiceParameters params) {
        return new OneToOnePersister(params);
    }

    private boolean ifCountHasChanged(CollectionAgent agent) {
        return (agent.getSavedIfCount() != -1) && (getIfNumber().getIfNumber() != agent.getSavedIfCount());
    }

 

}

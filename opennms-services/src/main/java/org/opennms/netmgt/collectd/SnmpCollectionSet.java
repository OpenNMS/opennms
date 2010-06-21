//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Sep 15: Create SnmpIfCollector even if only generic-index resource types exist and no interface ones (bug 2447) - jeffg@opennms.org
// 2008 Aug 29: Throw detailed failure exceptions in collect(). - dj@opennms.org
// 2006 Aug 15: Formatting, use generics for collections, be explicit about method visibility, add support for generic indexes. - dj@opennms.org
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
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class SnmpCollectionSet implements Collectable, CollectionSet {

    public static class RescanNeeded {
        boolean rescanNeeded = false;
        public void rescanIndicated() {
            rescanNeeded = true;
        }

        public boolean rescanIsNeeded() {
            return rescanNeeded;
        }

    }

    private final CollectionAgent m_agent;
    private final OnmsSnmpCollection m_snmpCollection;
    private SnmpIfCollector m_ifCollector;
    private IfNumberTracker m_ifNumber;
    private SysUpTimeTracker m_sysUpTime;
    private SnmpNodeCollector m_nodeCollector;
    private int m_status=ServiceCollector.COLLECTION_FAILED;
    private boolean m_ignorePersist;

    public String toString() {
    	StringBuffer buffer = new StringBuffer();

    	buffer.append("CollectionAgent: ");
    	buffer.append(m_agent);
    	buffer.append("\n");

    	buffer.append("OnmsSnmpCollection: ");
    	buffer.append(m_snmpCollection);
    	buffer.append("\n");

    	buffer.append("SnmpIfCollector: ");
    	buffer.append(m_ifCollector);
    	buffer.append("\n");

    	buffer.append("IfNumberTracker: ");
    	buffer.append(m_ifNumber);
    	buffer.append("\n");

        buffer.append("SysUpTimeTracker: ");
        buffer.append(m_sysUpTime);
        buffer.append("\n");

    	buffer.append("SnmpNodeCollector: ");
    	buffer.append(m_nodeCollector);
    	buffer.append("\n");

    	return buffer.toString();
    }

    public SnmpCollectionSet(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    public SnmpIfCollector getIfCollector() {
        if (m_ifCollector == null) {
            m_ifCollector = createIfCollector();
        }
        return m_ifCollector;
    }

    public IfNumberTracker getIfNumber() {
        if (m_ifNumber == null) {
            m_ifNumber = createIfNumberTracker();
        }
        return m_ifNumber;
    }

    public SysUpTimeTracker getSysUpTime() {
        if (m_sysUpTime == null) {
            m_sysUpTime = createSysUpTimeTracker();
        }
        return m_sysUpTime;
    }

    public SnmpNodeCollector getNodeCollector() {
        if (m_nodeCollector == null) {
            m_nodeCollector = createNodeCollector();
        }
        return m_nodeCollector;
    }

    private SnmpNodeCollector createNodeCollector() {
        SnmpNodeCollector nodeCollector = null;
        if (!getAttributeList().isEmpty()) {
            nodeCollector = new SnmpNodeCollector(m_agent.getInetAddress(), getAttributeList(), this);
        }
        return nodeCollector;
    }

    private IfNumberTracker createIfNumberTracker() {
        IfNumberTracker ifNumber = null;
        if (hasInterfaceDataToCollect()) {
            ifNumber = new IfNumberTracker();
        }
        return ifNumber;
    }

    private SysUpTimeTracker createSysUpTimeTracker() {
        SysUpTimeTracker sysUpTime = null;
        if (hasInterfaceDataToCollect()) {
            sysUpTime = new SysUpTimeTracker();
        }
        return sysUpTime;
    }

    private SnmpIfCollector createIfCollector() {
        SnmpIfCollector ifCollector = null;
        // construct the ifCollector
        if (hasInterfaceDataToCollect() || hasGenericIndexResourceDataToCollect()) {
            ifCollector = new SnmpIfCollector(m_agent.getInetAddress(), getCombinedIndexedAttributes(), this);
        }
        return ifCollector;
    }

    public NodeInfo getNodeInfo() {
        return getNodeResourceType().getNodeInfo();
    }

    boolean hasDataToCollect() {
        return (getNodeResourceType().hasDataToCollect() || getIfResourceType().hasDataToCollect() || hasGenericIndexResourceDataToCollect());
    }

    boolean hasInterfaceDataToCollect() {
        return getIfResourceType().hasDataToCollect();
    }
    
    boolean hasGenericIndexResourceDataToCollect() {
        return ! getGenericIndexResourceTypes().isEmpty();
    }

    public CollectionAgent getCollectionAgent() {
       return m_agent;
    }

    ThreadCategory log() {
       return ThreadCategory.getInstance(getClass());
    }

    Collection<SnmpAttributeType> getAttributeList() {
       return m_snmpCollection.getNodeResourceType(m_agent).getAttributeTypes();
    }

    List<SnmpAttributeType> getCombinedIndexedAttributes() {
    	List<SnmpAttributeType> attributes = new LinkedList<SnmpAttributeType>();

    	attributes.addAll(getIfResourceType().getAttributeTypes());
    	attributes.addAll(getIfAliasResourceType().getAttributeTypes());
    	attributes.addAll(getGenericIndexAttributeTypes());

    	return attributes;
    }

    protected Collection<SnmpAttributeType> getGenericIndexAttributeTypes() {
    	Collection<SnmpAttributeType> attributeTypes = new LinkedList<SnmpAttributeType>();
    	Collection<ResourceType> resourceTypes = getGenericIndexResourceTypes();
    	for (ResourceType resourceType : resourceTypes) {
    		attributeTypes.addAll(resourceType.getAttributeTypes());
    	}
    	return attributeTypes;
    }

    private Collection<ResourceType> getGenericIndexResourceTypes() {
        return m_snmpCollection.getGenericIndexResourceTypes(m_agent);
    }

    public CollectionTracker getCollectionTracker() {
        return new AggregateTracker(SnmpAttributeType.getCollectionTrackers(getAttributeTypes()));
    }

    private Collection<SnmpAttributeType> getAttributeTypes() {
        return m_snmpCollection.getAttributeTypes(m_agent);
    }

    public Collection<? extends CollectionResource> getResources() {
        return m_snmpCollection.getResources(m_agent);
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for (CollectionResource resource : getResources()) {
            resource.visit(visitor);
        }

        visitor.completeCollectionSet(this);
    }

    CollectionTracker getTracker() {
        List<Collectable> trackers = new ArrayList<Collectable>(4);

        if (getIfNumber() != null) {
        	trackers.add(getIfNumber());
        }
        if (getSysUpTime() != null) {
            trackers.add(getSysUpTime());
        }
        if (getNodeCollector() != null) {
        	trackers.add(getNodeCollector());
        }
        if (getIfCollector() != null) {
        	trackers.add(getIfCollector());
        }

        return new AggregateTracker(trackers);
    }

    protected SnmpWalker createWalker() {
        CollectionAgent agent = getCollectionAgent();
        return SnmpUtils.createWalker(getAgentConfig(), "SnmpCollectors for " + agent.getHostAddress(), getTracker());
    }

    private void logStartedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: successfully instantiated "
        					+ "SnmpNodeCollector() for "
        					+ getCollectionAgent().getHostAddress());
        }
    }

    private void logFinishedWalker() {
        log().info(
        		"collect: node SNMP query for address "
        				+ getCollectionAgent().getHostAddress() + " complete.");
    }

    /**
     * Log error and return COLLECTION_FAILED is there is a failure.
     * 
     * @param walker
     * @throws CollectionWarning
     */
    void verifySuccessfulWalk(SnmpWalker walker) throws CollectionException {
        if (!walker.failed()) {
            return;
        }

        if (walker.timedOut()) {
            throw new CollectionTimedOut(walker.getErrorMessage());
        }

        String message = "collection failed for "
            + getCollectionAgent().getHostAddress() 
            + " due to: " + walker.getErrorMessage();
        // Note: getErrorThrowable() return value can be null
        throw new CollectionWarning(message, walker.getErrorThrowable());
    }

    void collect() throws CollectionException {
        // XXX Should we have a call to hasDataToCollect here?
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

            m_status = ServiceCollector.COLLECTION_SUCCEEDED;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CollectionWarning("collect: Collection of node SNMP "
                    + "data for interface " + getCollectionAgent().getHostAddress()
                    + " interrupted: " + e, e);
        }
    }

    boolean checkDisableForceRescan(String disabledString) {
        String src = m_snmpCollection.getServiceParameters().getParameters().get("disableForceRescan");
        return ((src != null) && (src.toLowerCase().equals("all") || src.toLowerCase().equals(disabledString)));
    }

    void checkForNewInterfaces(SnmpCollectionSet.RescanNeeded rescanNeeded) {
        if (!hasInterfaceDataToCollect()) {
            return;
        }

        if (checkDisableForceRescan("ifnumber")) {
            log().info("checkForNewInterfaces: check rescan is disabled for node " + m_agent.getNodeId());
            return;
        }

        logIfCounts();

        if (getIfNumber().isChanged(getCollectionAgent().getSavedIfCount())) {
            log().info("Sending rescan event because the number of interfaces on primary SNMP "
            + "interface " + getCollectionAgent().getHostAddress()
            + " has changed, generating 'ForceRescan' event.");
            rescanNeeded.rescanIndicated();
        }

        getCollectionAgent().setSavedIfCount(getIfNumber().getIntValue());
    }

    void checkForSystemRestart(SnmpCollectionSet.RescanNeeded rescanNeeded) {
        if (!hasInterfaceDataToCollect()) {
            return;
        }

        if (checkDisableForceRescan("sysuptime")) {
            log().info("checkForSystemRestart: check rescan is disabled for node " + m_agent.getNodeId());
            return;
        }

        logSysUpTime();

    	m_ignorePersist = false;
        if (getSysUpTime().isChanged(getCollectionAgent().getSavedSysUpTime())) {
            log().info("Sending rescan event because sysUpTime has changed on primary SNMP "
            + "interface " + getCollectionAgent().getHostAddress()
            + ", generating 'ForceRescan' event.");
            rescanNeeded.rescanIndicated();
            /*
             * Only on sysUpTime change (i.e. SNMP Agent Restart) we must ignore collected data
             * to avoid spikes on RRD/JRB files
             */
            m_ignorePersist = true;
            getCollectionAgent().setSavedSysUpTime(-1);
        } else {
            getCollectionAgent().setSavedSysUpTime(getSysUpTime().getLongValue());
        }
    }

    private void logIfCounts() {
        if (log().isDebugEnabled()) {
            CollectionAgent agent = getCollectionAgent();
            log().debug("collect: nodeId: " + agent.getNodeId()
                    + " interface: " + agent.getHostAddress()
                    + " ifCount: " + getIfNumber().getIntValue()
                    + " savedIfCount: " + agent.getSavedIfCount());
        }
    }

    private void logSysUpTime() {
        if (log().isDebugEnabled()) {
            CollectionAgent agent = getCollectionAgent();
            log().debug("collect: nodeId: " + agent.getNodeId()
                    + " interface: " + agent.getHostAddress()
                    + " sysUpTime: " + getSysUpTime().getLongValue()
                    + " savedSysUpTime: " + agent.getSavedSysUpTime());
        }
    }

    public boolean rescanNeeded() {

        final RescanNeeded rescanNeeded = new RescanNeeded();
        visit(new ResourceVisitor() {

            public void visitResource(CollectionResource resource) {
                log().debug("rescanNeeded: Visiting resource " + resource);
                if (resource.rescanNeeded()) {
                    log().debug("Sending rescan event for "+getCollectionAgent()+" because resource "+resource+" indicated it was needed");
                    rescanNeeded.rescanIndicated();
                }
            }

        });

        checkForNewInterfaces(rescanNeeded);
        checkForSystemRestart(rescanNeeded);

        return rescanNeeded.rescanIsNeeded();
    }

    public SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig agentConfig = getCollectionAgent().getAgentConfig();
        agentConfig.setPort(m_snmpCollection.getSnmpPort(agentConfig.getPort()));
        agentConfig.setRetries(m_snmpCollection.getSnmpRetries(agentConfig.getRetries()));
        agentConfig.setTimeout(m_snmpCollection.getSnmpTimeout(agentConfig.getTimeout()));
        agentConfig.setReadCommunity(m_snmpCollection.getSnmpReadCommunity(agentConfig.getReadCommunity()));
        agentConfig.setWriteCommunity(m_snmpCollection.getSnmpWriteCommunity(agentConfig.getWriteCommunity()));
        agentConfig.setProxyFor(m_snmpCollection.getSnmpProxyFor(agentConfig.getProxyFor()));
        agentConfig.setVersion(m_snmpCollection.getSnmpVersion(agentConfig.getVersion()));
        agentConfig.setMaxVarsPerPdu(m_snmpCollection.getSnmpMaxVarsPerPdu(agentConfig.getMaxVarsPerPdu()));
        agentConfig.setMaxRepetitions(m_snmpCollection.getSnmpMaxRepetitions(agentConfig.getMaxRepetitions()));
        agentConfig.setMaxRequestSize(m_snmpCollection.getSnmpMaxRequestSize(agentConfig.getMaxRequestSize()));
        agentConfig.setSecurityName(m_snmpCollection.getSnmpSecurityName(agentConfig.getSecurityName()));
        agentConfig.setAuthPassPhrase(m_snmpCollection.getSnmpAuthPassPhrase(agentConfig.getAuthPassPhrase()));
        agentConfig.setAuthProtocol(m_snmpCollection.getSnmpAuthProtocol(agentConfig.getAuthProtocol()));
        agentConfig.setPrivPassPhrase(m_snmpCollection.getSnmpPrivPassPhrase(agentConfig.getPrivPassPhrase()));
        agentConfig.setPrivProtocol(m_snmpCollection.getSnmpPrivProtocol(agentConfig.getPrivProtocol()));
        return agentConfig;
    }

    public void notifyIfNotFound(AttributeDefinition attrType, SnmpResult res) {
        // Don't bother sending a rescan event in this case since localhost is not going to be there anyway
        //triggerRescan();
        log().info("Unable to locate resource for agent "+getCollectionAgent()+" with instance id "+res.getInstance()+" while collecting attribute "+attrType);
    }

    /* Not used anymore - done in CollectableService
     void saveAttributes(final ServiceParameters params) {
        BasePersister persister = createPersister(params);
        visit(persister);
    }

    private BasePersister createPersister(ServiceParameters params) {
        if (Boolean.getBoolean("org.opennms.rrd.storeByGroup")) {
            return new GroupPersister(params);
        } else {
            return new OneToOnePersister(params);
        }
    }*/

    private NodeResourceType getNodeResourceType() {
        return m_snmpCollection.getNodeResourceType(getCollectionAgent());
    }

    private IfResourceType getIfResourceType() {
        return m_snmpCollection.getIfResourceType(getCollectionAgent());
    }

    private IfAliasResourceType getIfAliasResourceType() {
        return m_snmpCollection.getIfAliasResourceType(getCollectionAgent());
    }

    public int getStatus() {
        return m_status;
    }

    public boolean ignorePersist() {
        return m_ignorePersist;
    }

}

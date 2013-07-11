/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.collector.AttributeDefinition;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SnmpCollectionSet class.</p>
 * 
 * After creation, be sure to call setCollectionTimestamp with the time the collection is taken
 * It is inappropriate to require it in the constructor, as instances may be created independently
 * and at a different time from when the data is collected.  (They're not currently, but it's better not to
 * make assumptions)
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpCollectionSet implements Collectable, CollectionSet {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSet.class);

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
    private Date m_timestamp;

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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

    /**
     * <p>Constructor for SnmpCollectionSet.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     */
    public SnmpCollectionSet(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    /**
     * <p>getIfCollector</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SnmpIfCollector} object.
     */
    public SnmpIfCollector getIfCollector() {
        if (m_ifCollector == null) {
            m_ifCollector = createIfCollector();
        }
        return m_ifCollector;
    }

    /**
     * <p>getIfNumber</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.IfNumberTracker} object.
     */
    public IfNumberTracker getIfNumber() {
        if (m_ifNumber == null) {
            m_ifNumber = createIfNumberTracker();
        }
        return m_ifNumber;
    }

    /**
     * <p>getSysUpTime</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SysUpTimeTracker} object.
     */
    public SysUpTimeTracker getSysUpTime() {
        if (m_sysUpTime == null) {
            m_sysUpTime = createSysUpTimeTracker();
        }
        return m_sysUpTime;
    }

    /**
     * <p>getNodeCollector</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.SnmpNodeCollector} object.
     */
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

    /**
     * <p>getNodeInfo</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.NodeInfo} object.
     */
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

    /**
     * <p>getCollectionAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public CollectionAgent getCollectionAgent() {
       return m_agent;
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

    /**
     * <p>getGenericIndexAttributeTypes</p>
     *
     * @return a {@link java.util.Collection} object.
     */
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

    /**
     * <p>getCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    @Override
    public CollectionTracker getCollectionTracker() {
        return new AggregateTracker(SnmpAttributeType.getCollectionTrackers(getAttributeTypes()));
    }

    private Collection<SnmpAttributeType> getAttributeTypes() {
        return m_snmpCollection.getAttributeTypes(m_agent);
    }

    /**
     * <p>getResources</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<? extends CollectionResource> getResources() {
        return m_snmpCollection.getResources(m_agent);
    }

    /** {@inheritDoc} */
    @Override
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

    /**
     * <p>createWalker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpWalker} object.
     */
    protected SnmpWalker createWalker() {
        CollectionAgent agent = getCollectionAgent();
        return SnmpUtils.createWalker(getAgentConfig(), "SnmpCollectors for " + agent.getHostAddress(), getTracker());
    }

    private void logStartedWalker() {
        LOG.debug("collect: successfully instantiated SnmpNodeCollector() for {}", getCollectionAgent().getHostAddress());
    }

    private void logFinishedWalker() {
        LOG.info("collect: node SNMP query for address {} complete.", getCollectionAgent().getHostAddress());
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

    boolean checkDisableForceRescan(final String disabledString) {
        final Map<String, Object> parameters = m_snmpCollection.getServiceParameters().getParameters();
        final String src = ParameterMap.getKeyedString(parameters, "disableForceRescan", null);
        return ((src != null) && (src.toLowerCase().equals("all") || src.toLowerCase().equals(disabledString)));
    }

    void checkForNewInterfaces(SnmpCollectionSet.RescanNeeded rescanNeeded) {
        if (!hasInterfaceDataToCollect()) {
            return;
        }

        if (checkDisableForceRescan("ifnumber")) {
            LOG.info("checkForNewInterfaces: check rescan is disabled for node {}", m_agent.getNodeId());
            return;
        }

        logIfCounts();

        if (getIfNumber().isChanged(getCollectionAgent().getSavedIfCount())) {
            LOG.info("Sending rescan event because the number of interfaces on primary SNMP interface {} has changed, generating 'ForceRescan' event.", getCollectionAgent().getHostAddress());
            rescanNeeded.rescanIndicated();
        }

        getCollectionAgent().setSavedIfCount(getIfNumber().getIntValue());
    }

    void checkForSystemRestart(SnmpCollectionSet.RescanNeeded rescanNeeded) {
        if (!hasInterfaceDataToCollect()) {
            return;
        }

        if (checkDisableForceRescan("sysuptime")) {
            LOG.info("checkForSystemRestart: check rescan is disabled for node {}", m_agent.getNodeId());
            return;
        }

        logSysUpTime();

    	m_ignorePersist = false;
        if (getSysUpTime().isChanged(getCollectionAgent().getSavedSysUpTime())) {
            LOG.info("Sending rescan event because sysUpTime has changed on primary SNMP interface {}, generating 'ForceRescan' event.", getCollectionAgent().getHostAddress());
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
        if (LOG.isDebugEnabled()) {
            CollectionAgent agent = getCollectionAgent();
            LOG.debug("collect: nodeId: {} interface: {} ifCount: {} savedIfCount: {}", agent.getNodeId(), agent.getHostAddress(), getIfNumber().getIntValue(), agent.getSavedIfCount());
        }
    }

    private void logSysUpTime() {
        if (LOG.isDebugEnabled()) {
            CollectionAgent agent = getCollectionAgent();
            LOG.debug("collect: nodeId: {} interface: {} sysUpTime: {} savedSysUpTime: {}", agent.getNodeId(), agent.getHostAddress(), getSysUpTime().getLongValue(), agent.getSavedSysUpTime());
        }
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    public boolean rescanNeeded() {

        final RescanNeeded rescanNeeded = new RescanNeeded();
        visit(new ResourceVisitor() {

            @Override
            public void visitResource(CollectionResource resource) {
                LOG.debug("rescanNeeded: Visiting resource {}", resource);
                if (resource.rescanNeeded()) {
                    LOG.debug("Sending rescan event for {} because resource {} indicated it was needed", getCollectionAgent(), resource);
                    rescanNeeded.rescanIndicated();
                }
            }

        });

        checkForNewInterfaces(rescanNeeded);
        checkForSystemRestart(rescanNeeded);

        return rescanNeeded.rescanIsNeeded();
    }

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
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

    /**
     * <p>notifyIfNotFound</p>
     *
     * @param attrType a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     * @param res a {@link org.opennms.netmgt.snmp.SnmpResult} object.
     */
    public void notifyIfNotFound(AttributeDefinition attrType, SnmpResult res) {
        // Don't bother sending a rescan event in this case since localhost is not going to be there anyway
        //triggerRescan();
        LOG.info("Unable to locate resource for agent {} with instance id {} while collecting attribute {}", getCollectionAgent(), res.getInstance(), attrType);
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

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return m_status;
    }

    /**
     * <p>ignorePersist</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean ignorePersist() {
        return m_ignorePersist;
    }

	@Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}

	public void setCollectionTimestamp(Date m_timestamp) {
		this.m_timestamp = m_timestamp;
	}

}

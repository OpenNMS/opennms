package org.opennms.netmgt.provision.service;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.BatchTask;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.updates.NodeUpdate;

public class BaseAgentScan {

    private NodeUpdate m_nodeUpdate;
    private Integer m_nodeId;
    private Date m_scanStamp = new Date();
    private final NodeScan m_nodeScan;
    
    BaseAgentScan(final Integer nodeId, final OnmsNode x, final NodeUpdate nodeUpdate, final NodeScan nodeScan) {
        m_nodeId = nodeId;
        m_nodeUpdate = nodeUpdate;
        m_nodeScan = nodeScan;
    }

    public Date getScanStamp() {
        return m_scanStamp;
    }

    public void setScanStamp(final Date date) {
    	m_scanStamp = date;
    }

    public OnmsNode getNode() {
        return m_nodeScan.getNode();
    }
    
    public void setNode(final OnmsNode node) {
    	m_nodeScan.setNode(node);
    }
    
    public NodeUpdate getNodeUpdate() {
    	return m_nodeUpdate;
    }
    
    public Integer getNodeId() {
        return m_nodeId;
    }

    public boolean isAborted() {
        return m_nodeScan.isAborted();
    }

    public void abort(final String reason) {
        m_nodeScan.abort(reason);
    }

    public String getForeignSource() {
        return getNode().getForeignSource();
    }

    public String getForeignId() {
        return getNode().getForeignId();
    }
    
    public ProvisionService getProvisionService() {
        return m_nodeScan.getProvisionService();
    }

    public EventForwarder getEventForwarder() {
    	return m_nodeScan.getEventForwarder();
    }

    public SnmpAgentConfigFactory getAgentConfigFactory() {
    	return m_nodeScan.getAgentConfigFactory();
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("foreign source", getForeignSource())
            .append("foreign id", getForeignId())
            .append("node id", m_nodeId)
            .append("scan stamp", m_scanStamp)
            .toString();
    }

    void updateIpInterface(final BatchTask currentPhase, final OnmsIpInterface iface) {
        getProvisionService().updateIpInterfaceAttributes(getNodeId(), iface);
        if (iface.isManaged()) {
            currentPhase.add(new IpInterfaceScan(getNodeId(), iface.getIpAddress(), getForeignSource(), getProvisionService()));
        }
    }

    protected Runnable ipUpdater(final BatchTask currentPhase, final OnmsIpInterface iface) {
        Runnable r = new Runnable() {
            public void run() {
                updateIpInterface(currentPhase, iface);
            }
        };
        return r;
    }
    
}
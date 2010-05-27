/**
 * 
 */
package org.opennms.netmgt.provision.service;

import java.net.InetAddress;
import java.util.List;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.util.Assert;

final class NodeInfoScan implements RunInBatch {

    private final SnmpAgentConfigFactory m_agentConfigFactory;
    private final InetAddress m_agentAddress;
    private final String m_foreignSource;
    private OnmsNode m_node;
    private final ProvisionService m_provisionService;
    private final ScanProgress m_scanProgress;
    

    NodeInfoScan(OnmsNode node, InetAddress agentAddress, String foreignSource, ScanProgress scanProgress, SnmpAgentConfigFactory agentConfigFactory, ProvisionService provisionService){
        m_node = node;
        m_agentAddress = agentAddress;
        m_foreignSource = foreignSource;
        m_scanProgress = scanProgress;
        m_agentConfigFactory = agentConfigFactory;
        m_provisionService = provisionService;
    }

    public void run(BatchTask phase) {
        
        phase.getBuilder().addSequence(
                new RunInBatch() {
                    public void run(BatchTask batch) {
                        collectNodeInfo();
                    }
                },
                new RunInBatch() {
                    public void run(BatchTask phase) {
                        doPersistNodeInfo();
                    }
                });
    }

    private InetAddress getAgentAddress() {
        return m_agentAddress;
    }

    private SnmpAgentConfig getAgentConfig(InetAddress primaryAddress) {
        return getAgentConfigFactory().getAgentConfig(primaryAddress);
    }

    private SnmpAgentConfigFactory getAgentConfigFactory() {
        return m_agentConfigFactory;
    }

    private String getForeignSource() {
        return m_foreignSource;
    }

    private ProvisionService getProvisionService() {
        return m_provisionService;
    }

    private void abort(String reason) {
        m_scanProgress.abort(reason);
    }

    private OnmsNode getNode() {
        return m_node;
    }

    private void setNode(OnmsNode node) {
        m_node = node;
    }

    private void collectNodeInfo() {
        Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");
        InetAddress primaryAddress = getAgentAddress();
        SnmpAgentConfig agentConfig = getAgentConfig(primaryAddress);
        
        SystemGroup systemGroup = new SystemGroup(primaryAddress);
        
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "systemGroup", systemGroup);
        walker.start();
        
        try {
        
            walker.waitFor();
        
            if (walker.timedOut()) {
                abort("Aborting node scan : Agent timedout while scanning the system table");
            }
            else if (walker.failed()) {
                abort("Aborting node scan : Agent failed while scanning the system table: " + walker.getErrorMessage());
            } else {
        
                systemGroup.updateSnmpDataForNode(getNode());
        
                List<NodePolicy> nodePolicies = getProvisionService().getNodePoliciesForForeignSource(getEffectiveForeignSource());
        
                OnmsNode node = getNode();
                for(NodePolicy policy : nodePolicies) {
                    if (node != null) {
                        node = policy.apply(node);
                    }
                }
        
                if (node == null) {
                    String reason = "Aborted scan of node due to configured policy";
                    abort(reason);
                } else {
                    setNode(node);
                }
        
            }
        } catch (InterruptedException e) {
            abort("Aborting node scan : Scan thread interrupted!");
        }
    }

    private String getEffectiveForeignSource() {
        return getForeignSource()  == null ? "default" : getForeignSource();
    }

    private void doPersistNodeInfo() {
        if (!isAborted()) {
            getProvisionService().updateNodeAttributes(getNode());
        }
    }

    private boolean isAborted() {
        return m_scanProgress.isAborted();
    }
}
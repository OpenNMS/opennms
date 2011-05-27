package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.LogUtils.debugf;

import java.util.List;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.NeedsContainer;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.updates.NodeUpdate;
import org.opennms.netmgt.provision.NodePolicy;

public class NoAgentScan extends BaseAgentScan implements NeedsContainer {

    NoAgentScan(final Integer nodeId, final OnmsNode node, final NodeUpdate nodeUpdate, final ProvisionService provisionService, final NodeScan parent) {
    	super(nodeId, node, nodeUpdate, parent);
    }
    
    private void applyNodePolicies(final BatchTask phase) {
    	final List<NodePolicy> nodePolicies = getProvisionService().getNodePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
        
    	OnmsNode node = getNode();
    	for(final NodePolicy policy : nodePolicies) {
            if (node != null) {
                node = policy.apply(node);
            }
        }
        
        if (node == null) {
        	abort("Aborted scan of node due to configured policy");
        } else {
            setNode(node);
        }
        
    }
    
    void stampProvisionedInterfaces(final BatchTask phase) {
        if (!isAborted()) { 
        
            for(final OnmsIpInterface iface : getNode().getIpInterfaces()) {
                iface.setIpLastCapsdPoll(getScanStamp());
                phase.add(ipUpdater(phase, iface), "write");
        
            }
        
        }
    }

    void deleteObsoleteResources(final BatchTask phase) {
        getProvisionService().updateNodeScanStamp(getNodeId(), getScanStamp());
        getProvisionService().deleteObsoleteInterfaces(getNodeId(), getScanStamp());
    }
    
    private void doPersistNodeInfo(final BatchTask phase) {
        if (!isAborted()) {
            getProvisionService().updateNodeAttributes(getNode(), getNodeUpdate());
        }
        debugf(this, "Finished phase %s", phase);
    }

    public void run(final ContainerTask<?> parent) {
        parent.getBuilder().addSequence(
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        applyNodePolicies(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        stampProvisionedInterfaces(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        deleteObsoleteResources(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        doPersistNodeInfo(phase);
                    }
                }
        );
    }
    
}
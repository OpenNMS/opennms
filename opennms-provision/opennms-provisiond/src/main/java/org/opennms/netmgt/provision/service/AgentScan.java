package org.opennms.netmgt.provision.service;

import static org.opennms.core.utils.LogUtils.debugf;
import static org.opennms.core.utils.LogUtils.infof;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.ContainerTask;
import org.opennms.core.tasks.NeedsContainer;
import org.opennms.core.tasks.RunInBatch;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.updates.NodeUpdate;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TableTracker;
import org.springframework.util.Assert;

/**
 * AgentScan
 *
 * @author brozow
 */
public class AgentScan extends BaseAgentScan implements NeedsContainer, ScanProgress {

    private InetAddress m_agentAddress;
    private String m_agentType;

    public AgentScan(final Integer nodeId, final OnmsNode node, final NodeUpdate nodeUpdate, final InetAddress agentAddress, final String agentType, ProvisionService x, NodeScan parent) {
        super(nodeId, node, nodeUpdate, parent);
        m_agentAddress = agentAddress;
        m_agentType = agentType;
    }
    
    public InetAddress getAgentAddress() {
        return m_agentAddress;
    }
    
    public String getAgentType() {
        return m_agentType;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("address", m_agentAddress)
            .append("type", m_agentType)
            .toString();
    }
    
    void completed() {
        if (!isAborted()) {
        	final EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "Provisiond");
            bldr.setNodeid(getNodeId());
            bldr.setInterface(getAgentAddress());
            getEventForwarder().sendNow(bldr.getEvent());
        }
    }

    void deleteObsoleteResources() {
        if (!isAborted()) {
            getProvisionService().updateNodeScanStamp(getNodeId(), getScanStamp());
            getProvisionService().deleteObsoleteInterfaces(getNodeId(), getScanStamp());
            debugf(this, "Finished deleteObsoleteResources for %s", this);
        }
    }

    public void detectIpAddressTable(final BatchTask currentPhase) {
    	final OnmsNode node = getNode();

		// mark all provisioned interfaces as 'in need of scanning' so we can mark them
        // as scanned during ipAddrTable processing
        final Set<InetAddress> provisionedIps = new HashSet<InetAddress>();
        if (getForeignSource() != null) {
            for(final OnmsIpInterface provisioned : node.getIpInterfaces()) {
                provisionedIps.add(provisioned.getIpAddress());
            }
        }
        
        final IPAddressTableTracker ipAddressTracker = new IPAddressTableTracker() {
        	@Override
        	public void processIPAddressRow(final IPAddressRow row) {
        		final String ipAddress = row.getIpAddress();
				infof(this, "Processing IPAddress table row with ipAddr %s", ipAddress);
        		
        		if (ipAddress != null && !ipAddress.startsWith("127.0.0.") && !ipAddress.equals("0000:0000:0000:0000:0000:0000:0000:0001")) {
                    // mark any provisioned interface as scanned
                    provisionedIps.remove(ipAddress);

                    OnmsIpInterface iface = row.createInterfaceFromRow();
                    iface.setIpLastCapsdPoll(getScanStamp());
                    iface.setIsManaged("M");

                    final List<IpInterfacePolicy> policies = getProvisionService().getIpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                    for(final IpInterfacePolicy policy : policies) {
                        if (iface != null) {
                            iface = policy.apply(iface);
                        }
                    }

                    if (iface != null) {
                        currentPhase.add(ipUpdater(currentPhase, iface), "write");
                    }
        		}
        	}
        };

        walkTable(currentPhase, provisionedIps, ipAddressTracker);
    }
    
    public void detectIpInterfaceTable(final BatchTask currentPhase) {
    	final OnmsNode node = getNode();

		// mark all provisioned interfaces as 'in need of scanning' so we can mark them
        // as scanned during ipAddrTable processing
        final Set<InetAddress> provisionedIps = new HashSet<InetAddress>();
        if (getForeignSource() != null) {
            for(final OnmsIpInterface provisioned : node.getIpInterfaces()) {
                provisionedIps.add(provisioned.getIpAddress());
            }
        }

        final IPInterfaceTableTracker ipIfTracker = new IPInterfaceTableTracker() {
        	@Override
        	public void processIPInterfaceRow(final IPInterfaceRow row) {
        		final String ipAddress = row.getIpAddress();
        		infof(this, "Processing IPInterface table row with ipAddr %s for node %d/%s/%s", ipAddress, node.getId(), node.getForeignSource(), node.getForeignId());
        		if (ipAddress != null && !ipAddress.startsWith("127.0.0.") && !ipAddress.equals("0000:0000:0000:0000:0000:0000:0000:0001")) {

                    // mark any provisioned interface as scanned
                    provisionedIps.remove(ipAddress);

                    // save the interface
                    OnmsIpInterface iface = row.createInterfaceFromRow();
                    iface.setIpLastCapsdPoll(getScanStamp());

                    // add call to the ip interface is managed policies
                    iface.setIsManaged("M");

                    final List<IpInterfacePolicy> policies = getProvisionService().getIpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                    for(final IpInterfacePolicy policy : policies) {
                        if (iface != null) {
                            iface = policy.apply(iface);
                        }
                    }

                    if (iface != null) {
                        currentPhase.add(ipUpdater(currentPhase, iface), "write");
                    }

                }
            }
        };

        walkTable(currentPhase, provisionedIps, ipIfTracker);
    }

	private void walkTable(final BatchTask currentPhase, final Set<InetAddress> provisionedIps, final TableTracker tracker) {
        final OnmsNode node = getNode();
		infof(this, "detecting IP interfaces for node %d/%s/%s using table tracker %s", node.getId(), node.getForeignSource(), node.getForeignId(), tracker);

		if (isAborted()) {
			debugf(this, "'%s' is marked as aborted; skipping scan of table %s", currentPhase, tracker);
		} else {
            Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");

        	final SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());

			final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "IP address tables", tracker);
			walker.start();
      
			try {
			    walker.waitFor();
      
			    if (walker.timedOut()) {
			        abort("Aborting node scan : Agent timed out while scanning the IP address tables");
			    }
			    else if (walker.failed()) {
			        abort("Aborting node scan : Agent failed while scanning the IP address tables : " + walker.getErrorMessage());
			    } else {
      
			        // After processing the snmp provided interfaces then we need to scan any that 
			        // were provisioned but missing from the ip table
			        for(final InetAddress ipAddr : provisionedIps) {
			            final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddr);
			            iface.setIpLastCapsdPoll(getScanStamp());
			            iface.setIsManaged("M");
      
			            currentPhase.add(ipUpdater(currentPhase, iface), "write");
      
			        }
      
			        debugf(this, "Finished phase %s", currentPhase);
      
			    }
			} catch (final InterruptedException e) {
			    abort("Aborting node scan : Scan thread failed while waiting for the IP address tables");
			}
		}
	}
    
    public void detectPhysicalInterfaces(final BatchTask currentPhase) {
        if (isAborted()) { return; }
        final SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(getAgentAddress());
        Assert.notNull(getAgentConfigFactory(), "agentConfigFactory was not injected");
        
        final PhysInterfaceTableTracker physIfTracker = new PhysInterfaceTableTracker() {
            @Override
            public void processPhysicalInterfaceRow(PhysicalInterfaceRow row) {
            	infof(this, "Processing ifTable row for ifIndex %d on node %d/%s/%s", row.getIfIndex(), getNodeId(), getForeignSource(), getForeignId());
            	OnmsSnmpInterface snmpIface = row.createInterfaceFromRow();
                snmpIface.setLastCapsdPoll(getScanStamp());
                
                final List<SnmpInterfacePolicy> policies = getProvisionService().getSnmpInterfacePoliciesForForeignSource(getForeignSource() == null ? "default" : getForeignSource());
                for(final SnmpInterfacePolicy policy : policies) {
                    if (snmpIface != null) {
                        snmpIface = policy.apply(snmpIface);
                    }
                }
                
                if (snmpIface != null) {
                    final OnmsSnmpInterface snmpIfaceResult = snmpIface;
    
                    // add call to the snmp interface collection enable policies
    
                    final Runnable r = new Runnable() {
                        public void run() {
                            getProvisionService().updateSnmpInterfaceAttributes(getNodeId(), snmpIfaceResult);
                        }
                    };
                    currentPhase.add(r, "write");
                }
            }
        };
        
        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable/ifXTable", physIfTracker);
        walker.start();
        
        try {
            walker.waitFor();
    
            if (walker.timedOut()) {
                abort("Aborting node scan : Agent timed out while scanning the interfaces table");
            }
            else if (walker.failed()) {
                abort("Aborting node scan : Agent failed while scanning the interfaces table: " + walker.getErrorMessage());
            }
            else {
                debugf(this, "Finished phase %s", currentPhase);
            }
        } catch (final InterruptedException e) {
            abort("Aborting node scan : Scan thread interrupted while waiting for interfaces table");
        }
    }

    public void run(final ContainerTask<?> parent) {
        parent.getBuilder().addSequence(
                new NodeInfoScan(getNode(),getAgentAddress(), getForeignSource(), this, getAgentConfigFactory(), getProvisionService(), getNodeId()),
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        detectIpAddressTable(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        detectIpInterfaceTable(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        detectPhysicalInterfaces(phase);
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        deleteObsoleteResources();
                    }
                },
                new RunInBatch() {
                    public void run(final BatchTask phase) {
                        completed();
                    }
                }
        );
    }
}
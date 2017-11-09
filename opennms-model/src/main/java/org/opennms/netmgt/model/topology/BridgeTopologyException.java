package org.opennms.netmgt.model.topology;

public class BridgeTopologyException extends Exception implements BridgeTopology {

    /**
     * 
     */
    private static final long serialVersionUID = -6913989384724814658L;

    BridgeTopology m_topology;
    public BridgeTopologyException(String message, BridgeTopology topology) {
        super(message);
        m_topology=topology;
    }

    public BridgeTopologyException(String message,BridgeTopology topology, Throwable throwable) {
        super(message, throwable);
        m_topology=topology;
    }
    
    public String printTopology() {
        return m_topology.printTopology();
    }

}

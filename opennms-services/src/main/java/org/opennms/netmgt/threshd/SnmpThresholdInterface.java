package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.poller.NetworkInterface;

public class SnmpThresholdInterface {
    
    private static final String SNMP_THRESH_IFACE_KEY = SnmpThresholdInterface.class.getName();

    public static SnmpThresholdInterface get(NetworkInterface iface) {
        
        SnmpThresholdInterface snmpIface = (SnmpThresholdInterface) iface.getAttribute(SNMP_THRESH_IFACE_KEY);
        if (snmpIface == null) {
            snmpIface = new SnmpThresholdInterface(iface);
            iface.setAttribute(SNMP_THRESH_IFACE_KEY, snmpIface);
        }
        
        return snmpIface;
    }

    private NetworkInterface m_netInterface;
    /**
     * Interface attribute key used to store the interface's node id
     */
    static final String NODE_ID_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.NodeId";
    /**
     * We must maintain a map of interface level ThresholdEntity objects on a
     * per interface basis in order to maintain separate exceeded counts and the
     * like for each of a node's interfaces. This interface attribute key used
     * to store a map of interface level ThresholdEntity object maps keyed by
     * ifLabel. So it wil refer to a map of maps indexed by ifLabel.
     */
    static final String ALL_IF_THRESHOLD_MAP_KEY = "org.opennms.netmgt.collectd.SnmpThresholder.AllIfThresholdMap";

    

    public SnmpThresholdInterface(NetworkInterface iface) {
        m_netInterface = iface;
    }

    public NetworkInterface getNetworkInterface() {
        return m_netInterface;
    }

    boolean isIPV4() {
        return getNetworkInterface().getType() == NetworkInterface.TYPE_IPV4;
    }

    InetAddress getInetAddress() {
        return (InetAddress) getNetworkInterface().getAddress();
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<String, ThresholdEntity>> getAllInterfaceMap() {
        return (Map<String, Map<String, ThresholdEntity>>)getNetworkInterface().getAttribute(SnmpThresholdInterface.ALL_IF_THRESHOLD_MAP_KEY);
    }

    void setAllInterfaceMap(Map<String, Map<String, ThresholdEntity>> allInterfaceMap) {
        getNetworkInterface().setAttribute(SnmpThresholdInterface.ALL_IF_THRESHOLD_MAP_KEY, allInterfaceMap);
    }

    String getIpAddress() {
        return getInetAddress().getHostAddress();
    }

    void setNodeId(int nodeId) {
        getNetworkInterface().setAttribute(SnmpThresholdInterface.NODE_ID_KEY, new Integer(nodeId));
    }

    Integer getNodeId() {
        return (Integer) getNetworkInterface().getAttribute(SnmpThresholdInterface.NODE_ID_KEY);
    }



}

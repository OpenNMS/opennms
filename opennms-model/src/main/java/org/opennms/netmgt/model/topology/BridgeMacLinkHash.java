package org.opennms.netmgt.model.topology;

import org.opennms.netmgt.model.BridgeMacLink;

public class BridgeMacLinkHash {
    final Integer nodeid;
    final Integer bridgeport;
    final String mac;
    public BridgeMacLinkHash(BridgeMacLink maclink) {
        super();
        nodeid = maclink.getNode().getId();
        bridgeport = maclink.getBridgePort();
        mac = maclink.getMacAddress();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bridgeport == null) ? 0 : bridgeport.hashCode());
        result = prime * result + ((mac == null) ? 0 : mac.hashCode());
        result = prime * result + ((nodeid == null) ? 0 : nodeid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgeMacLinkHash other = (BridgeMacLinkHash) obj;
        if (bridgeport == null) {
            if (other.bridgeport != null)
                return false;
        } else if (!bridgeport.equals(other.bridgeport))
            return false;
        if (mac == null) {
            if (other.mac != null)
                return false;
        } else if (!mac.equals(other.mac))
            return false;
        if (nodeid == null) {
            if (other.nodeid != null)
                return false;
        } else if (!nodeid.equals(other.nodeid))
            return false;
        return true;
    }
    

}

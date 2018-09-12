package org.opennms.netmgt.model;

import java.util.Set;

public interface OnmsTopologyConsumer extends OnmsTopologyRef {
    
    Set<OnmsTopologyProtocol> getProtocols();

    void consume(OnmsTopologyMessage message);
    
}

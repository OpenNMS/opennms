package org.opennms.netmgt.topologies.service.api;

import java.util.Comparator;

public class OnmsTopologyProtocolComparator implements Comparator<OnmsTopologyProtocol> {

    @Override
    public int compare(OnmsTopologyProtocol o1, OnmsTopologyProtocol o2) {
        if (o1.getLayer().getPosition() != o2.getLayer().getPosition()) {
            return o1.getLayer().getPosition() - o2.getLayer().getPosition();
        }            
        return o1.getId().compareTo(o2.getId());
    }
    
}
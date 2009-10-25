package org.opennms.netmgt.provision.adapters.link;

import java.util.Collection;

import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;

public interface NodeLinkService {
    
    public String getNodeLabel(int nodeId);
    public void createLink(int nodeParentId, int nodeId);
    public void saveLinkState(OnmsLinkState state);
    public Integer getNodeId(String endPoint);
    public Collection<DataLinkInterface> getLinkContainingNodeId(int nodeId);
    public void updateLinkStatus(int nodeParentId, int nodeId, String status);
    public OnmsLinkState getLinkStateForInterface(DataLinkInterface dataLinkInterface);
    public String getPrimaryAddress(int nodeId);

}

package org.opennms.netmgt.provision;

public interface NodeLinkService {
    
    public String getNodeLabel(int nodeId);
    public void createLink(int nodeParentId, int nodeId);
    public Integer getNodeId(String endPoint);
    public void updateLinkStatus(int nodeParentId, int nodeId, String status);
    
}

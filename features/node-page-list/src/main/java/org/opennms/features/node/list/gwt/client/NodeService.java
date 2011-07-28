package org.opennms.features.node.list.gwt.client;

import com.google.gwt.http.client.RequestCallback;

public interface NodeService {
    
    public void getAllIpInterfacesForNode(int nodeId, RequestCallback callback);
    public void getAllSnmpInterfacesForNode(int nodeId, RequestCallback callback);
    public void findIpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback);
    public void findSnmpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback);
}

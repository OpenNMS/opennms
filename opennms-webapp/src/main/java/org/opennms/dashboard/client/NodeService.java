package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface NodeService extends RemoteService {

    public String[] getNodeNames();
    
}

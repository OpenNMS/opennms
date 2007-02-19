package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface NodeServiceAsync {
    
    public void getNodeNames(AsyncCallback cb);

}

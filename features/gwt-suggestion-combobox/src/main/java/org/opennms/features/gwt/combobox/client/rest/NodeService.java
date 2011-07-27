package org.opennms.features.gwt.combobox.client.rest;

import com.google.gwt.http.client.RequestCallback;

public interface NodeService {
    public void getAllNodes(RequestCallback callback);
    public void getNodeByNodeLabel(String nodeLabel, RequestCallback callback);
    
}

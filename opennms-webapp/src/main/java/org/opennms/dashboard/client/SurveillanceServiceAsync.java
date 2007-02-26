package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous interface for SurveillanceService.
 */
public interface SurveillanceServiceAsync {
    public void getSurveillanceData(AsyncCallback cb);
    
    public void getAlarmsForSet(SurveillanceSet set, AsyncCallback cb);
    
    public void getNodeNames(AsyncCallback cb);
    
    public void getResources(AsyncCallback cb);

    public void getChildResources(String resourceId, AsyncCallback cb);

    public void getPrefabGraphs(String resourceId, AsyncCallback cb);
}

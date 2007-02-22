package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SurveillanceServiceAsync {
    public void getSurveillanceData(AsyncCallback cb);
    
    public void getAlarmsForSet(SurveillanceSet set, AsyncCallback cb);
}

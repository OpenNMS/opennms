package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface SurveillanceService extends RemoteService {

    public SurveillanceData getSurveillanceData();
    
    public Alarm[] getAlarmsForSet(SurveillanceSet set);
    
    public String[] getNodeNames();
    
    public String[][] getResources();
    
    public String[][] getChildResources(String resourceId);
    
    public String[][] getPrefabGraphs(String resourceId);
}

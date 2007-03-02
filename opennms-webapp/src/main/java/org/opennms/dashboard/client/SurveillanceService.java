package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.RemoteService;

public interface SurveillanceService extends RemoteService {

    public SurveillanceData getSurveillanceData();
    
    public Alarm[] getAlarmsForSet(SurveillanceSet set);
    
    public Notification[] getNotificationsForSet(SurveillanceSet set);
    
    public String[] getNodeNames(SurveillanceSet set);
    
    public String[][] getResources(SurveillanceSet set);
    
    public String[][] getChildResources(String resourceId);
    
    public String[][] getPrefabGraphs(String resourceId);
}

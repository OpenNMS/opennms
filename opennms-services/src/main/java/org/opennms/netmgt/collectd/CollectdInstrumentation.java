package org.opennms.netmgt.collectd;

public interface CollectdInstrumentation {
    
    public void beginScheduleExistingInterfaces();
    public void endScheduleExistingInterfaces();
    public void beginScheduleInterfacesWithService(String svcName);
    public void endScheduleInterfacesWithService(String svcName);
    public void beginFindInterfacesWithService(String svcName);
    public void endFindInterfacesWithService(String svcName, int count);
    public void beginScheduleInterface(int nodeId, String ipAddress, String svcName);
    public void endScheduleInterface(int nodeId, String ipAddress, String svcName);
    public void beginCollectorInitialize(int nodeId, String ipAddress, String svcName);
    public void endCollectorInitialize(int nodeId, String ipAddress, String svcName);
    public void beginCollectorRelease(int nodeId, String ipAddress, String svcName);
    public void endCollectorRelease(int nodeId, String ipAddress, String svcName);
    public void beginCollectorCollect(int nodeId, String ipAddress, String svcName);
    public void endCollectorCollect(int nodeId, String ipAddress, String svcName);
    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName);
    public void endCollectingServiceData(int nodeId, String ipAddress, String svcName);
    public void beginPersistingServiceData(int nodeId, String ipAddress, String svcName);
    public void endPersistingServiceData(int nodeId, String ipAddress, String svcName);
    public void reportCollectionError(int nodeid, String ipAddress, String svcName, CollectionError e);
    

}

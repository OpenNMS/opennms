package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

public class DefaultCollectdInstrumentation implements CollectdInstrumentation {

    private Category log() {
        return Logger.getLogger("Instrumentation.Collectd");
    }

    public void beginScheduleExistingInterfaces() {
        log().debug("scheduleExistingInterfaces: begin");
    }

    public void endScheduleExistingInterfaces() {
        log().debug("scheduleExistingInterfaces: end");
    }

    public void beginScheduleInterfacesWithService(String svcName) {
        log().debug("scheduleInterfacesWithService: begin: "+svcName);
    }

    public void endScheduleInterfacesWithService(String svcName) {
        log().debug("scheduleInterfacesWithService: end: "+svcName);
    }

    public void beginFindInterfacesWithService(String svcName) {
        log().debug("scheduleFindInterfacesWithService: begin: "+svcName);
    }

    public void endFindInterfacesWithService(String svcName, int count) {
        log().debug("scheduleFindInterfacesWithService: end: "+svcName+". found "+count+" interfaces.");
    }

    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.collect: collectData: begin: "+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void endCollectingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: collectData: end: "+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void beginCollectorCollect(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: begin:"+nodeId+"/"+ipAddress+"/"+svcName);
    }

    public void endCollectorCollect(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.collect: end:"+nodeId+"/"+ipAddress+"/"+svcName);
        
    }

    public void beginCollectorRelease(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.release: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endCollectorRelease(int nodeId, String ipAddress, String svcName) {
        log().debug("collector.release: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: persistDataQueueing: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.collect: persistDataQueueing: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.initialize: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("collector.initialize: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void beginScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("scheduleInterfaceWithService: begin: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("scheduleInterfaceWithService: end: "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void reportCollectionError(int nodeId, String ipAddress,
            String svcName, CollectionError e) {
        log().debug("collector.collect: error: "+nodeId+"/"+ipAddress+"/"+svcName+": "+e);
    }

}

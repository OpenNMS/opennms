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
        log().debug("schedulingExistingInterfaces: end");
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
        log().debug("begin "+svcName+" collector.release for if "+nodeId+"/"+ipAddress);

    }

    public void endCollectorRelease(int nodeId, String ipAddress, String svcName) {
        log().debug("end "+svcName+" collector.release for if "+nodeId+"/"+ipAddress);

    }

    public void beginPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("begin persisting "+svcName+" data for if "+nodeId+"/"+ipAddress);

    }

    public void endPersistingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("end persisting "+svcName+" data for if "+nodeId+"/"+ipAddress);

    }

    public void beginCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("begin "+svcName+" collector.initialize for if "+nodeId+"/"+ipAddress);

    }

    public void endCollectorInitialize(int nodeId, String ipAddress,
            String svcName) {
        log().debug("end "+svcName+" collector.initialize for if "+nodeId+"/"+ipAddress);

    }

    public void beginScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("begin schedule interface for svc "+nodeId+"/"+ipAddress+"/"+svcName);

    }

    public void endScheduleInterface(int nodeId, String ipAddress,
            String svcName) {
        log().debug("end schedule interface for svc "+nodeId+"/"+ipAddress+"/"+svcName);

    }

}

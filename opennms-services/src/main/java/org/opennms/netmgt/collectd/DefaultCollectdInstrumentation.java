package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class DefaultCollectdInstrumentation implements CollectdInstrumentation {

    public void beginScheduleExistingInterfaces() {
        log().debug("begin scheduling interfaces");
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void endScheduleExistingInterfaces() {
        log().debug("end scheduling interfaces");
    }

    public void beginScheduleInterfacesWithService(String svcName) {
        log().debug("begin scheduling interfaces with service: "+svcName);
    }

    public void endScheduleInterfacesWithService(String svcName) {
        log().debug("end scheduling interfaces with service: "+svcName);
    }

    public void beginFindInterfacesWithService(String svcName) {
        log().debug("begin find interfaces with service: "+svcName);
    }

    public void endFindInterfacesWithService(String svcName, int count) {
        log().debug("end find interfaces with service: "+svcName+". found "+count+" interfaces.");
    }

    public void beginCollectingServiceData(int nodeId, String ipAddress, String svcName) {
        log().debug("begin collecting "+svcName+" data for if "+nodeId+"/"+ipAddress);
    }

    public void endCollectingServiceData(int nodeId, String ipAddress,
            String svcName) {
        log().debug("end collecting "+svcName+" data for if "+nodeId+"/"+ipAddress);
    }

    public void beginCollectorCollect(int nodeId, String ipAddress,
            String svcName) {
        log().debug("begin "+svcName+" collector.collect for if "+nodeId+"/"+ipAddress);
    }

    public void endCollectorCollect(int nodeId, String ipAddress, String svcName) {
        log().debug("end "+svcName+" collector.collect for if "+nodeId+"/"+ipAddress);
        
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

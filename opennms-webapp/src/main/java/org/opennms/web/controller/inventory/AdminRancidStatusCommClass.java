package org.opennms.web.controller.inventory;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

public class AdminRancidStatusCommClass {
        
    private String groupName;
    
    private String deviceName;
    
    private String statusName;

    
    public String getStatusName() {
        return statusName;
    }
    
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private static Category log() {
        return Logger.getLogger("Rancid");
    }

}

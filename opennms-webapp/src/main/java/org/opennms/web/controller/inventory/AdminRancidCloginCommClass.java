package org.opennms.web.controller.inventory;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

public class AdminRancidCloginCommClass {

    private String userID;
    private String pass;
    private String enpass;
    private String loginM;
    private String autoE;
    private String groupName;
    private String deviceName;
    
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEnpass() {
        return enpass;
    }

    public void setEnpass(String enpass) {
        this.enpass = enpass;
    }

    public String getLoginM() {
        return loginM;
    }

    public void setLoginM(String loginM) {
        this.loginM = loginM;
    }

    public String getAutoE() {
        return autoE;
    }

    public void setAutoE(String autoE) {
        this.autoE = autoE;
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

package org.opennms.web.svclayer.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opennms.rancid.InventoryElement2;

public class NodeBaseInventory {

    String devicename;
    String groupname;
    String version;
    String status;
    Date creationdate;
    String swconfigurationurl;
    String configurationurl;

    List<InventoryElement2> ie;
    
    public NodeBaseInventory(){
        ie = new ArrayList<InventoryElement2>();
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    public String getSwconfigurationurl() {
        return swconfigurationurl;
    }

    public void setSwconfigurationurl(String swconfigurationurl) {
        this.swconfigurationurl = swconfigurationurl;
    }

    public String getConfigurationurl() {
        return configurationurl;
    }

    public void setConfigurationurl(String configurationurl) {
        this.configurationurl = configurationurl;
    }

    public List<InventoryElement2> getIe() {
        return ie;
    }

    public void setIe(List<InventoryElement2> ie) {
        this.ie = ie;
    }
    
    public String expand () {
        
        Iterator<InventoryElement2> iter1 = ie.iterator();

        String tot="";
        while (iter1.hasNext()){
            InventoryElement2 tmp = iter1.next();
            tot = tot + "<" + tmp.expand()+ ">\n";
        }
        return tot;

    }

    
}


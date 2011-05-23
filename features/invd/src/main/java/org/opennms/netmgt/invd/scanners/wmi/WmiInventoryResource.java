package org.opennms.netmgt.invd.scanners.wmi;

import org.opennms.netmgt.invd.InventoryResource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WmiInventoryResource implements InventoryResource {
    private String resourceName;
    private String resourceSource;
    private Integer ownerNodeId;
    private Date resourceDate;
    private String resourceCategory;
    private final HashMap<String, String> resourceProperties = new HashMap<String, String>();

    public boolean rescanNeeded() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Date getResourceDate() {
        return resourceDate;
    }

    public void setResourceDate(Date resourceDate) {
        this.resourceDate = resourceDate;
    }

    public String getResourceSource() {
        return resourceSource;
    }

    public void setResourceSource(String resourceSource) {
        this.resourceSource = resourceSource;
    }

    public Integer getOwnerNodeId() {
        return ownerNodeId;
    }

    public void setOwnerNodeId(Integer ownerNodeId) {
        this.ownerNodeId = ownerNodeId;
    }

    public String getResourceCategory() {
        return resourceCategory;
    }

    public void setResourceCategory(String resourceCategory) {
        this.resourceCategory = resourceCategory;
    }

    public Map<String,String> getResourceProperties() {
        return this.resourceProperties;
    }
}

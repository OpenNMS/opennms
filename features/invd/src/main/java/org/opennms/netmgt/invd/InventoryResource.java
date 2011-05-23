package org.opennms.netmgt.invd;

import java.util.Date;
import java.util.Map;

public interface InventoryResource {
    //public boolean shouldPersist(ServiceParameters params);

    public boolean rescanNeeded();

    //public void visit(CollectionSetVisitor visitor);

    public String getResourceName();
    public Date getResourceDate();
    public String getResourceSource();
    public Integer getOwnerNodeId();
    public Map<String, String> getResourceProperties();
    public String getResourceCategory();
}

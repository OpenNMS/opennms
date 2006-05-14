package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpValue;


public abstract class CollectionResource {
    
    private ResourceType m_resourceType;

    private Map m_groups = new HashMap();

    public CollectionResource(ResourceType def) {
        m_resourceType = def;
    }
    
    public ResourceType getResourceType() {
        return m_resourceType;
    }

    public abstract CollectionAgent getCollectionAgent();

    public abstract Collection getAttributeTypes();
    
    public abstract boolean shouldPersist(ServiceParameters params);

    protected abstract File getResourceDir(RrdRepository repository);
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public boolean rescanNeeded() { return false; }
    
    protected void storeAttributes(final RrdRepository repository) {
        visit(new AttributeVisitor() {

            public void visitAttribute(Attribute attribute) {
                attribute.storeAttribute(repository);
            }

        });
    }

    public void setAttributeValue(AttributeType type, SnmpValue val) {
        Attribute attr = new Attribute(this, type, val);
        addAttribute(attr);
    }

    private void addAttribute(Attribute attr) {
        AttributeGroup group = getGroup(attr.getGroupName());
        group.addAttribute(attr);
    }

    private AttributeGroup getGroup(String groupName) {
        AttributeGroup group = (AttributeGroup)m_groups.get(groupName);
        if (group == null) {
            group = new AttributeGroup(this, groupName);
            m_groups.put(group.getName(), group);
        }
        return group;
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        
        for (Iterator it = getGroups().iterator(); it.hasNext();) {
            AttributeGroup group = (AttributeGroup) it.next();
            group.visit(visitor);
        }
    }

    private Collection getGroups() {
        return m_groups.values();
    }

}

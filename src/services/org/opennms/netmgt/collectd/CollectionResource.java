package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpValue;


public abstract class CollectionResource {
    
    private ResourceType m_resourceType;

    private Collection m_attrSet = new HashSet();

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
    
    protected void storeAttributes(RrdRepository repository) {
        /*
         * Iterate over the resource attribute list and issue RRD
         * update commands to update each datasource which has a
         * corresponding value in the collected SNMP data.
         */
        for (Iterator iter = getAttributes().iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            attr.storeAttribute(repository);
    
        }
    }

    public void setAttributeValue(AttributeType type, SnmpValue val) {
        Attribute attr = new Attribute(this, type, val);
        m_attrSet.add(attr);
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        
        for (Iterator it = getAttributes().iterator(); it.hasNext();) {
            Attribute attr = (Attribute) it.next();
            attr.visitAttribute(visitor);
        }
    }

    private Collection getAttributes() {
       return m_attrSet;
    }
    
    

}

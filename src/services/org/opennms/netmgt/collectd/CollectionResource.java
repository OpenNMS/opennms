package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpValue;


public abstract class CollectionResource {
    
    private ResourceType m_resourceType;

    private Collection m_attrSet;

    public CollectionResource(ResourceType def) {
        m_resourceType = def;
    }
    
    public ResourceType getResourceType() {
        return m_resourceType;
    }

    public abstract CollectionAgent getCollectionAgent();

    public abstract Collection getAttributeList();
    
    public abstract boolean shouldPersist(ServiceParameters params);

    protected abstract File getResourceDir(File rrdBaseDir);
    
    /**
     * @deprecated
     */
    protected abstract SNMPCollectorEntry getEntry();

    /**
     * @deprecated 
     */
    public abstract void setEntry(SNMPCollectorEntry entry);

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public boolean rescanNeeded() { return false; }
    
    protected void logNoDataForAttribute(AttributeType attrType) {
        if (log().isDebugEnabled()) {
            log().debug(
        			"updateRRDs: Skipping update, "
        					+ "no data retrieved for resource: " + this + 
                            " attribute: " + attrType.getName());
        }
    }

    protected void logUpdateFailed(AttributeType attrType) {
        log().warn("updateRRDs: ds.performUpdate() failed for resource: "
                + this
                + " datasource: "
                + attrType.getName());
    }

    protected void store(AttributeType attrType, File rrdBaseDir) {
        CollectionAgent collectionAgent = getCollectionAgent();
        if (attrType.performUpdate(collectionAgent, getResourceDir(rrdBaseDir), getEntry())) {
            logUpdateFailed(attrType);
        }
    }

    protected void logUpdateException(AttributeType attrType, IllegalArgumentException e) {
        log().warn("updateRRDs: exception saving data for resource: " + this
        			+ " datasource: " + attrType.getName(), e);
    }

    protected void storeAttributes(File rrdBaseDir) {
        /*
         * Iterate over the resource attribute list and issue RRD
         * update commands to update each datasource which has a
         * corresponding value in the collected SNMP data.
         */
        Iterator i = getAttributeList().iterator();
        while (i.hasNext()) {
            AttributeType attrType = (AttributeType)i.next();
            try {
                if (attrType.getValue(getEntry()) == null) {
                    logNoDataForAttribute(attrType);
                } else {
                    store(attrType, rrdBaseDir);
                }
            } catch (IllegalArgumentException e) {
                logUpdateException(attrType, e);
            }
    
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


public abstract class CollectionResource {
    
    public abstract CollectionAgent getCollectionAgent();

    public abstract List getAttributeList();
    
    public abstract boolean shouldPersist(ServiceParameters params);

    protected abstract File getResourceDir(File rrdBaseDir);
    
    protected abstract SNMPCollectorEntry getEntry();

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    protected void logNoDataForAttribute(CollectionAttribute attr) {
        if (log().isDebugEnabled()) {
            log().debug(
        			"updateRRDs: Skipping update, "
        					+ "no data retrieved for resource: " + this + 
                            " attribute: " + attr.getName());
        }
    }

    protected void logUpdateFailed(CollectionAttribute attr) {
        log().warn("updateRRDs: ds.performUpdate() failed for resource: "
                + this
                + " datasource: "
                + attr.getName());
    }

    protected void store(CollectionAttribute attr, File rrdBaseDir) {
        CollectionAgent collectionAgent = getCollectionAgent();
        if (attr.performUpdate(collectionAgent, getResourceDir(rrdBaseDir), getEntry())) {
            logUpdateFailed(attr);
        }
    }

    protected void logUpdateException(CollectionAttribute attr, IllegalArgumentException e) {
        log().warn("updateRRDs: exception saving data for resource: " + this
        			+ " datasource: " + attr.getName(), e);
    }

    protected void storeAttributes(File rrdBaseDir) {
        /*
         * Iterate over the resource attribute list and issue RRD
         * update commands to update each datasource which has a
         * corresponding value in the collected SNMP data.
         */
        Iterator i = getAttributeList().iterator();
        while (i.hasNext()) {
            CollectionAttribute attr = (CollectionAttribute)i.next();
            try {
                if (attr.getValue(getEntry()) == null) {
                    logNoDataForAttribute(attr);
                } else {
                    store(attr, rrdBaseDir);
                }
            } catch (IllegalArgumentException e) {
                logUpdateException(attr, e);
            }
    
        }
    }

}

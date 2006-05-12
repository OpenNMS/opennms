package org.opennms.netmgt.collectd;

public abstract class ResourceVisitor extends AbstractCollectionSetVisitor {
    
    abstract public void visitResource(CollectionResource resource);

}

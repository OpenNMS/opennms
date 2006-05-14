package org.opennms.netmgt.collectd;

public abstract class AttributeVisitor extends AbstractCollectionSetVisitor {
    
    abstract public void visitAttribute(Attribute attribute);


}

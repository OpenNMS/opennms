package org.opennms.netmgt.collectd;

public interface CollectionSetVisitor {

    void visitCollectionSet(CollectionSet set);

    void visitResource(CollectionResource resource);

    void visitAttribute(Attribute attribute);

}

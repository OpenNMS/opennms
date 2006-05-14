package org.opennms.netmgt.collectd;

public interface CollectionSetVisitor {

    void visitCollectionSet(CollectionSet set);

    void visitResource(CollectionResource resource);

    void visitGroup(AttributeGroup group);

    void visitAttribute(Attribute attribute);

}

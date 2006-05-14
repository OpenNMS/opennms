package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AttributeGroup {
    
    private CollectionResource m_resource;
    private String m_name;
    private Set m_attributes = new HashSet();
    
    public AttributeGroup(CollectionResource resource, String name) {
        m_resource = resource;
        m_name = name;
    }

    public String getName() {
        return m_name;
    }
    
    public CollectionResource getResource() {
        return m_resource;
    }
    
    public Collection getAttributes() {
        return m_attributes;
    }
    
    public void addAttribute(Attribute attr) {
        m_attributes.add(attr);
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitGroup(this);
        
        for (Iterator iter = getAttributes().iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            attr.visit(visitor);
        }
    }
    
}

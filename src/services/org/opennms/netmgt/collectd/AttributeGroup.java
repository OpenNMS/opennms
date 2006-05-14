package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class AttributeGroup {
    
    private CollectionResource m_resource;
    private String m_name;
    private Set m_attributes = new HashSet();
    private String m_ifType;
    
    public AttributeGroup(CollectionResource resource, String name, String ifType) {
        m_resource = resource;
        m_name = name;
        m_ifType = ifType;
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
        
        visitor.completeGroup(this);
    }
    
    public boolean shouldPersist(ServiceParameters params) {
        if ("ignored".equals(m_ifType)) return true;
        if ("all".equals(m_ifType)) return true;
        
        String type = String.valueOf(m_resource.getType());
        
        if (type.equals(m_ifType)) return true;
        
        StringTokenizer tokenizer = new StringTokenizer(m_ifType, ",");
        while(tokenizer.hasMoreTokens()) {
            if (type.equals(tokenizer.nextToken()))
                return true;
        }
        return false;   
 
        
    }
    
}

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Iterator;

public class AliasedGroup extends AttributeGroup {
	
	AttributeGroup m_group;

	public AliasedGroup(CollectionResource resource, AttributeGroup group) {
		super(resource, group.getGroupType());
		m_group = group;
	}

	public void addAttribute(Attribute attr) {
		m_group.addAttribute(attr);
	}

	public boolean equals(Object obj) {
		return m_group.equals(obj);
	}

	public Collection getAttributes() {
		return m_group.getAttributes();
	}

	public AttributeGroupType getGroupType() {
		return m_group.getGroupType();
	}

	public String getName() {
		return m_group.getName();
	}

	public int hashCode() {
		return m_group.hashCode();
	}

	public boolean shouldPersist(ServiceParameters params) {
		return m_group.shouldPersist(params);
	}

	public String toString() {
		return m_group.toString();
	}

	public void visit(CollectionSetVisitor visitor) {
		visitor.visitGroup(this);
		
		for (Iterator iter = getAttributes().iterator(); iter.hasNext();) {
		    Attribute attr = (Attribute) iter.next();
		    Attribute aliased = new Attribute(getResource(), attr.getAttributeType(), attr.getValue());
		    aliased.visit(visitor);
		}
		
		visitor.completeGroup(this);
	}

}

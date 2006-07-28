package org.opennms.netmgt.collectd;

import org.apache.log4j.Category;
import org.opennms.netmgt.snmp.SnmpValue;

public class AliasedAttribute extends Attribute {
	
	public AliasedAttribute(CollectionResource resource, Attribute attr) {
		super(resource, attr.getAttributeType(), attr.getValue());
		m_attr = attr;
	}

	private Attribute m_attr;

	public boolean equals(Object obj) {
		return m_attr.equals(obj);
	}

	public AttributeType getAttributeType() {
		return m_attr.getAttributeType();
	}

	public String getName() {
		return m_attr.getName();
	}

	public String getType() {
		return m_attr.getType();
	}

	public SnmpValue getValue() {
		return m_attr.getValue();
	}

	public int hashCode() {
		return m_attr.hashCode();
	}

	public Category log() {
		return m_attr.log();
	}

	public boolean shouldPersist(ServiceParameters params) {
		return m_attr.shouldPersist(params);
	}

    public String toString() {
        return getResource()+"."+getAttributeType()+" = "+getValue();
    }

	

}

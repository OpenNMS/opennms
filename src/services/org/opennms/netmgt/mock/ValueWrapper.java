package org.opennms.netmgt.mock;

import org.opennms.netmgt.xml.event.Value;

public class ValueWrapper {
	Value m_value;
	
	public ValueWrapper(Value value) {
		m_value = value;
	}
	
	public Value getValue() {
		return m_value;
	}
	
	public String toString() {
		return m_value.getType() + "(" + m_value.getEncoding() + "): " + m_value.getContent();
	}
}

package org.opennms.netmgt.model.updates;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class FieldUpdate<T> {
	private String m_fieldName;

	private T m_value;
	private boolean m_set = false;

	public FieldUpdate(final String fieldName) {
		m_fieldName = fieldName;
	}

	public boolean isSet() {
		return m_set;
	}

	public T get() {
		return m_value;
	}

	public void set(final T value) {
		m_set  = true;
		m_value = value;
	}
	
	public void apply(final Object obj) {
		if (m_set) {
			BeanWrapper wrapper = new BeanWrapperImpl(obj);
			wrapper.setPropertyValue(m_fieldName, m_value);
		}
	}
}

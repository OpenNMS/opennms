package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAttribute;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public abstract class MobileSequenceOperation {
	public MobileSequenceOperation() {
	}

	public MobileSequenceOperation(String label) {
		setLabel(label);
	}
	
	private String m_label;
	
	@XmlAttribute(name="label")
	public String getLabel() {
		return m_label;
	}
	public void setLabel(String label) {
		m_label = label;
	}
	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}
}

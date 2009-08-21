package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="transaction")
public class SynchronousSequenceTransaction extends AbstractSequenceTransaction {
	@XmlAttribute(name="type", required=false)
	private String m_type = "synchronous";
	
	public SynchronousSequenceTransaction() {
		super();
	}

	public SynchronousSequenceTransaction(String label) {
		super();
		setLabel(label);
	}

	public String getType() {
		return m_type;
	}
}

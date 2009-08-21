package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="transaction")
public class AsynchronousSequenceTransaction extends AbstractSequenceTransaction {
	@XmlAttribute(name="type")
	private String m_type = "asynchronous";

	public AsynchronousSequenceTransaction() {
		super();
	}

	public AsynchronousSequenceTransaction(String label) {
		super();
		setLabel(label);
	}

	public String getType() {
		return m_type;
	}
}

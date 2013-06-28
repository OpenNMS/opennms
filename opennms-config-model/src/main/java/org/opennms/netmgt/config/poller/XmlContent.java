package org.opennms.netmgt.config.poller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poller-configuration.xsd")
public class XmlContent {
	
	@XmlValue
	private String content;
	
	public XmlContent() {}
	
	public XmlContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}

}

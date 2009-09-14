package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ussd-response")
public class UssdSequenceResponse extends MobileSequenceResponse {

	public UssdSequenceResponse() {
		super();
	}
	
	public UssdSequenceResponse(String label) {
		super(label);
	}
}

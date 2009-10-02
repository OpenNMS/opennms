package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sms-response")
public class SmsSequenceResponse extends MobileSequenceResponse {
	
	public SmsSequenceResponse() {
		super();
	}
	
	public SmsSequenceResponse(String label) {
		super(label);
	}
	
	public SmsSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}
}

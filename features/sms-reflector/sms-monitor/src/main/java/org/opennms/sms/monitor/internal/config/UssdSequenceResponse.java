package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;

@XmlRootElement(name="ussd-response")
public class UssdSequenceResponse extends MobileSequenceResponse {

    public UssdSequenceResponse() {
		super();
	}
	
	public UssdSequenceResponse(String label) {
		super(label);
	}
	
	public UssdSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}

	@Override
    protected boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response) {
        return response instanceof UssdResponse;
    }

    @Override
    public void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        if (response instanceof UssdResponse) {
            
        }
    }



}

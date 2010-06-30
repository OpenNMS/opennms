package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.SmsResponse;

/**
 * <p>SmsSequenceResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="sms-response")
public class SmsSequenceResponse extends MobileSequenceResponse {
	
    /**
     * <p>Constructor for SmsSequenceResponse.</p>
     */
    public SmsSequenceResponse() {
		super();
	}
	
	/**
	 * <p>Constructor for SmsSequenceResponse.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public SmsSequenceResponse(String label) {
		super(label);
	}
	
	/**
	 * <p>Constructor for SmsSequenceResponse.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public SmsSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}

	/** {@inheritDoc} */
	@Override
    protected boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response) {
        return response instanceof SmsResponse;
    }

    /** {@inheritDoc} */
    @Override
    public void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        if (response instanceof SmsResponse) {
            SmsResponse smsResponse = (SmsResponse)response;
            
            session.setVariable(getEffectiveLabel(session)+".smsOriginator", smsResponse.getOriginator());
        }
    }

	

}

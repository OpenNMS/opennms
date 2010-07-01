package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;

/**
 * <p>UssdSequenceResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="ussd-response")
public class UssdSequenceResponse extends MobileSequenceResponse {

    /**
     * <p>Constructor for UssdSequenceResponse.</p>
     */
    public UssdSequenceResponse() {
		super();
	}
	
	/**
	 * <p>Constructor for UssdSequenceResponse.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public UssdSequenceResponse(String label) {
		super(label);
	}
	
	/**
	 * <p>Constructor for UssdSequenceResponse.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public UssdSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}

	/** {@inheritDoc} */
	@Override
    protected boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response) {
        return response instanceof UssdResponse;
    }

    /** {@inheritDoc} */
    @Override
    public void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        if (response instanceof UssdResponse) {
            
        }
    }



}

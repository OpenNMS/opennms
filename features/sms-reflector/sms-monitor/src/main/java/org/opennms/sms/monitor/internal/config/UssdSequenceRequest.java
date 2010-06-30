package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;

/**
 * <p>UssdSequenceRequest class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="ussd-request")
public class UssdSequenceRequest extends MobileSequenceRequest {

	/**
	 * <p>Constructor for UssdSequenceRequest.</p>
	 */
	public UssdSequenceRequest() {
		super();
	}
	
	/**
	 * <p>Constructor for UssdSequenceRequest.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public UssdSequenceRequest(String label, String text) {
		super(label, text);
	}

	/**
	 * <p>Constructor for UssdSequenceRequest.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 * @param text a {@link java.lang.String} object.
	 */
	public UssdSequenceRequest(String gatewayId, String label, String text) {
		super(gatewayId, label, text);
	}

    /** {@inheritDoc} */
    @Override
    public void send(MobileSequenceSession session, MobileMsgResponseHandler responseHandler) {
        session.sendUssd(getGatewayIdForRequest(), getText(), responseHandler);
    }

}

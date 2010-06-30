package org.opennms.sms.reflector.smsservice;

import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;

/**
 * <p>OnmsInboundMessageNotification interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsInboundMessageNotification {

	/**
	 * <p>process</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @param msgType a {@link org.smslib.Message.MessageTypes} object.
	 * @param msg a {@link org.smslib.InboundMessage} object.
	 */
	void process(final AGateway gateway, final MessageTypes msgType, final InboundMessage msg);
}

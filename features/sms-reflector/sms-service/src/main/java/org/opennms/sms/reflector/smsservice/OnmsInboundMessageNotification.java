package org.opennms.sms.reflector.smsservice;

import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;

public interface OnmsInboundMessageNotification {

	void process(final AGateway gateway, final MessageTypes msgType, final InboundMessage msg);
}

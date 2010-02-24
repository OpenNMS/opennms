package org.opennms.sms.monitor;

import java.util.Date;
import java.util.Queue;

import org.opennms.protocols.rt.Messenger;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.SmsResponse;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.smslib.InboundMessage;
import org.smslib.USSDDcs;
import org.smslib.USSDResponse;
import org.smslib.USSDSessionStatus;

/**
 * @author brozow
 *
 */
public class TestMessenger implements Messenger<MobileMsgRequest, MobileMsgResponse> {
    
    protected Queue<MobileMsgResponse> m_q;

    /* (non-Javadoc)
     * @see org.opennms.protocols.rt.Messenger#sendRequest(java.lang.Object)
     */
    public void sendRequest(MobileMsgRequest request) throws Exception {
        // fake send this
        request.setSendTimestamp(System.currentTimeMillis());
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.rt.Messenger#start(java.util.Queue)
     */
    public void start(Queue<MobileMsgResponse> q) {
        m_q = q;
    }
    
    public void sendTestResponse(MobileMsgResponse response) {
        m_q.offer(response);
    }

    /**
     * @param msg1
     */
    public void sendTestResponse(InboundMessage msg) {
        sendTestResponse(new SmsResponse(msg, System.currentTimeMillis()));
    }

    /**
     * @param response
     */
    public void sendTestResponse(String gatewayId, USSDResponse response) {
        sendTestResponse(new UssdResponse(gatewayId, response, System.currentTimeMillis()));
    }

    public USSDResponse sendTestResponse(final String gatewayId, String content, USSDSessionStatus status) {
        USSDResponse r = new USSDResponse();
        r.setContent(content);
        r.setUSSDSessionStatus(status);
        r.setDcs(USSDDcs.UNSPECIFIED_7BIT);
        
        sendTestResponse(gatewayId, r);
        
        return r;
    }

    public InboundMessage sendTestResponse(String recipient, String text) {
        InboundMessage responseMsg = new InboundMessage(new Date(), recipient, text, 0, "0");
        sendTestResponse(responseMsg);
        return responseMsg;
    }

}
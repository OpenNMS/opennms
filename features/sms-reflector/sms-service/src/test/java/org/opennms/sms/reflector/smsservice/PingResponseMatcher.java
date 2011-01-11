package org.opennms.sms.reflector.smsservice;



/**
 * PingResponseMatcher
 *
 * @author brozow
 */
class PingResponseMatcher implements MobileMsgResponseMatcher {
    public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
        
        if (!(request instanceof SmsRequest)) return false;
        if (!(response instanceof SmsResponse)) return false;
        
        
        SmsRequest smsRequest = (SmsRequest) request;
        SmsResponse smsResponse = (SmsResponse) response;
        
        return smsRequest.getRecipient().equals(smsResponse.getOriginator()) 
            && "pong".equalsIgnoreCase(smsResponse.getText());
    }
}
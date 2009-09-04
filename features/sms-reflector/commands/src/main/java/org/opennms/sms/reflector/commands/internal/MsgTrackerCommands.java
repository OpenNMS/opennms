package org.opennms.sms.reflector.commands.internal;

import java.util.concurrent.CountDownLatch;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseCallback;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.opennms.sms.reflector.smsservice.SmsResponse;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.opennms.sms.reflector.smsservice.UssdResponse;
import org.smslib.OutboundMessage;
import org.smslib.USSDRequest;

import com.sun.org.apache.regexp.internal.recompile;

/**
 * Public API representing an example OSGi service
 */
public class MsgTrackerCommands implements CommandProvider
{
    private MobileMsgTracker m_tracker;
    
    private static class MsgCallback implements MobileMsgResponseCallback {

        MobileMsgResponse m_response;
        CountDownLatch m_latch = new CountDownLatch(1);

        public void handleError(MobileMsgRequest request, Throwable t) {
            t.printStackTrace();
            m_latch.countDown();
        }

        public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
            m_response = response;
            m_latch.countDown();
            return true;
        }

        public void handleTimeout(MobileMsgRequest request) {
            System.err.println("Request " + request + " timed out!"); 
            m_latch.countDown();
        }
        
        public void waitFor() throws InterruptedException {
            m_latch.await();
        }
        
        public MobileMsgResponse getResponse() {
            return m_response = m_response;
        }
        
    }
    
    private static class MsgMatcher implements MobileMsgResponseMatcher {
        
        String m_regex;
        
        public MsgMatcher(String regex) {
            m_regex = regex;
        }

        public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
            System.err.println("Using regex: " + m_regex + " to match response: " + response );
            boolean retVal = false;
            if (response instanceof SmsResponse) {
               retVal =  ((SmsResponse)response).getText().matches(m_regex);
            }
            else if (response instanceof UssdResponse) {
                retVal = ((UssdResponse)response).getContent().matches(m_regex);
            }
            System.err.println("Mathing: " + retVal + " for regex " + m_regex + " response " + response);
            return retVal;
        }
        
    }
    
    
    public void setMobileMsgTracker(MobileMsgTracker tracker) {
        m_tracker = tracker;
    }
    
    
    public void _trackSms(CommandInterpreter intp) {
        
        try {
            String recipient = intp.nextArgument();
            String text = intp.nextArgument();
            String regex = intp.nextArgument();

            if (recipient == null || text == null || regex == null) {
                intp.println("usage: trackSms <recipient> <msg> <response-regexp>");
                return;
            }

            OutboundMessage msg = new OutboundMessage(recipient, text);

            MsgCallback cb = new MsgCallback();
            m_tracker.sendSmsRequest(msg, 60000, 0, cb, new MsgMatcher(regex));

            cb.waitFor();

            intp.println("Response: "+ cb.getResponse());

        } catch (Exception e) {
            intp.printStackTrace(e);
        }
        
    }
    
    public void _trackUssd(CommandInterpreter intp) {
        
        try {
            String gatewayId = intp.nextArgument();
            String text = intp.nextArgument();
            String regex = intp.nextArgument();

            if (gatewayId == null || text == null || regex == null) {
                intp.println("usage: trackUssd <gateway> <msg> <response-regexp>");
                return;
            }

            USSDRequest msg = new USSDRequest(text);

            MsgCallback cb = new MsgCallback();
            m_tracker.sendUssdRequest(gatewayId, msg, 60000, 0, cb, new MsgMatcher(regex));

            cb.waitFor();

            intp.println("Response: "+ cb.getResponse());

        } catch (Exception e) {
            intp.printStackTrace(e);
        }
    }
    
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---Msg Tracker Commands---");
        buffer.append("\n\t").append("trackSms <recipient> <msg> <regexp>");
        buffer.append("\n\t").append("trackUssd <gateway> <msg> <regexp>");
        buffer.append("\n");
        return buffer.toString(); 
    } 

}


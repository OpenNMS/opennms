package org.opennms.sms.reflector.commands.internal;

import java.util.concurrent.CountDownLatch;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseCallback;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.smslib.OutboundMessage;
import org.smslib.USSDRequest;
import org.opennms.core.utils.ThreadCategory;

/**
 * Public API representing an example OSGi service
 *
 * @author ranger
 * @version $Id: $
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
           tracef("Request %s timed out!", request); 
            m_latch.countDown();
        }
        
        public void waitFor() throws InterruptedException {
            m_latch.await();
        }
        
        public MobileMsgResponse getResponse() {
            return m_response;
        }
        
    }
    
    private static class MsgMatcher implements MobileMsgResponseMatcher {
        
        String m_regex;
        
        public MsgMatcher(String regex) {
            m_regex = regex;
        }

        public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
            tracef("Using regex: %s to match response: %s", m_regex, response );
            boolean retVal = response.getText().matches(m_regex);
            tracef("Matching: %s for regex %s response %s", retVal, m_regex, response);
            return retVal;
        }
        
    }
    
    
    /**
     * <p>setMobileMsgTracker</p>
     *
     * @param tracker a {@link org.opennms.sms.reflector.smsservice.MobileMsgTracker} object.
     */
    public void setMobileMsgTracker(MobileMsgTracker tracker) {
        m_tracker = tracker;
    }
    
    
    /**
     * <p>_trackSms</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     */
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
    
    /**
     * <p>_trackUssd</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     */
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
            msg.setGatewayId(gatewayId);

            MsgCallback cb = new MsgCallback();
            m_tracker.sendUssdRequest(msg, 60000, 0, cb, new MsgMatcher(regex));

            cb.waitFor();

            intp.println("Response: "+ cb.getResponse());

        } catch (Exception e) {
            intp.printStackTrace(e);
        }
    }
    
    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---Msg Tracker Commands---");
        buffer.append("\n\t").append("trackSms <recipient> <msg> <regexp>");
        buffer.append("\n\t").append("trackUssd <gateway> <msg> <regexp>");
        buffer.append("\n");
        return buffer.toString(); 
    } 
    
    /**
     * <p>tracef</p>
     *
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void tracef(String format, Object... args) {
        ThreadCategory log = ThreadCategory.getInstance(MobileMsgResponseMatchers.class);
        
        if (log.isTraceEnabled()) {
            log.trace(String.format(format, args));
        }
    }

}


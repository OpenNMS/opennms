/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.commands.internal;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.builder.ToStringBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Public API representing an example OSGi service
 *
 * @author ranger
 * @version $Id: $
 */
public class MsgTrackerCommands implements CommandProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MsgTrackerCommands.class);
    
    private MobileMsgTracker m_tracker;
    
    private static class MsgCallback implements MobileMsgResponseCallback {

        MobileMsgResponse m_response;
        CountDownLatch m_latch = new CountDownLatch(1);

        @Override
        public void handleError(final MobileMsgRequest request, final Throwable t) {
            t.printStackTrace();
            LOG.warn("failed request: {}", request, t);
            m_latch.countDown();
        }

        @Override
        public boolean handleResponse(MobileMsgRequest request, MobileMsgResponse response) {
            m_response = response;
            m_latch.countDown();
            return true;
        }

        @Override
        public void handleTimeout(MobileMsgRequest request) {
           LOG.trace("Request {} timed out!", request); 
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

        @Override
        public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
            LOG.trace("Using regex: {} to match response: {}", m_regex, response );
            boolean retVal = response.getText().matches(m_regex);
            LOG.trace("Matching: {} for regex {} response {}", retVal, m_regex, response);
            return retVal;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("regex", m_regex)
                .toString();
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

        } catch (Throwable e) {
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

        } catch (Throwable e) {
            intp.printStackTrace(e);
        }
    }
    
    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---Msg Tracker Commands---");
        buffer.append("\n\t").append("trackSms <recipient> <msg> <regexp>");
        buffer.append("\n\t").append("trackUssd <gateway> <msg> <regexp>");
        buffer.append("\n");
        return buffer.toString(); 
    } 
}


/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.HostaddrMatch;
import org.opennms.netmgt.config.syslogd.HostnameMatch;
import org.opennms.netmgt.config.syslogd.ParameterAssignment;
import org.opennms.netmgt.config.syslogd.ProcessMatch;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

/**
 * This routine does the majority of Syslogd's work.
 * Improvements most likely are to be made.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
final class ConvertToEvent {
    private static final Logger LOG = LoggerFactory.getLogger(ConvertToEvent.class);

    /** Constant <code>HIDDEN_MESSAGE="The message logged has been removed due"{trunked}</code> */
    protected static final String HIDDEN_MESSAGE = "The message logged has been removed due to configuration of Syslogd; it may contain sensitive data.";

    /**
     * The received XML event, decoded using the US-ASCII encoding.
     */
    private final String m_eventXML;

    /**
     * The Internet address of the sending agent.
     */
    private final InetAddress m_sender;

    /**
     * The port of the agent on the remote system.
     */
    private final int m_port;

    /**
     * The list of event that have been acknowledged.
     */
    private final List<Event> m_ackEvents = new ArrayList<Event>();

    private Event m_event;

    private static Class<? extends SyslogParser> m_parserClass = null;

    private static Map<String,Pattern> m_patterns = new ConcurrentHashMap<String,Pattern>();

    /**
     * Private constructor to prevent the used of <em>new</em> except by the
     * <code>make</code> method.
     * 
     * @param eventXml 
     * @param port 
     * @param addr 
     */
    private ConvertToEvent(InetAddress addr, int port, String eventXml) {
        m_sender = addr;
        m_port = port;
        m_eventXML = eventXml;
    }

    public static void invalidate() {
        m_parserClass = null;
        m_patterns.clear();
    }

    /**
     * Constructs a new event encapsulation instance based upon the
     * information passed to the method. The passed datagram data is decoded
     * into a string using the <tt>US-ASCII</tt> character encoding.
     *
     * @param packet The datagram received from the remote agent.
     * @throws java.io.UnsupportedEncodingException
     *          Thrown if the data buffer cannot be decoded using the
     *          US-ASCII encoding.
     * @throws MessageDiscardedException 
     */
    static ConvertToEvent make(final DatagramPacket packet, final String matchPattern, final int hostGroup, final int messageGroup, final UeiList ueiList, final HideMessage hideMessage, final String discardUei)
            throws UnsupportedEncodingException, MessageDiscardedException {
        return make(packet.getAddress(), packet.getPort(), packet.getData(), packet.getLength(), matchPattern, hostGroup, messageGroup, ueiList, hideMessage, discardUei);
    }

    /**
     * Constructs a new event encapsulation instance based upon the
     * information passed to the method. The passed byte array is decoded into
     * a string using the <tt>US-ASCII</tt> character encoding.
     *
     * @param addr The remote agent's address.
     * @param port The remote agent's port
     * @param data The XML data in US-ASCII encoding.
     * @param len  The length of the XML data in the buffer.
     * @throws java.io.UnsupportedEncodingException
     *          Thrown if the data buffer cannot be decoded using the
     *          US-ASCII encoding.
     * @throws MessageDiscardedException 
     */
    static ConvertToEvent make(final InetAddress addr, final int port, final byte[] data,
                               final int len, final String matchPattern, final int hostGroup, final int messageGroup,
                               final UeiList ueiList, final HideMessage hideMessage, final String discardUei)
            throws UnsupportedEncodingException, MessageDiscardedException {
        if (m_parserClass == null) {
            final String parser = SyslogdConfigFactory.getInstance().getParser();
            try {
                m_parserClass = Class.forName(parser).asSubclass(SyslogParser.class);
            } catch (final Exception ex) {
                LOG.debug("Unable to instantiate Syslog parser class specified in config: {}", parser, ex);
                m_parserClass = CustomSyslogParser.class;
            }
        }

        String deZeroedData = new String(data, 0, len, "US-ASCII");
        if (deZeroedData.endsWith("\0")) {
            deZeroedData = deZeroedData.substring(0, deZeroedData.length() - 1);
        }

        final ConvertToEvent e = new ConvertToEvent(addr, port, deZeroedData);

        LOG.debug("Converting to event: {}", e);

        final SyslogParser parser;
        try {
            Method m = m_parserClass.getDeclaredMethod("getParser", String.class);
            Object[] args = new Object[] { e.m_eventXML };
            parser = (SyslogParser)m.invoke(ConvertToEvent.class, args);
        } catch (final Exception ex) {
            LOG.debug("Unable to get parser for class '{}'", m_parserClass.getName(), ex);
            throw new MessageDiscardedException(ex);
        }

        if (!parser.find()) {
            throw new MessageDiscardedException("message does not match");
        }
        SyslogMessage message;
        try {
            message = parser.parse();
        } catch (final SyslogParserException ex) {
            LOG.debug("Unable to parse '{}'", e.m_eventXML, ex);
            throw new MessageDiscardedException(ex);
        }

        LOG.debug("got syslog message {}", message);
        if (message == null) {
            throw new MessageDiscardedException(String.format("Unable to parse '%s'", e.m_eventXML));
        }
        // Build a basic event out of the syslog message
        final String priorityTxt = message.getSeverity().toString();
        final String facilityTxt = message.getFacility().toString();

        EventBuilder bldr = new EventBuilder("uei.opennms.org/syslogd/" + facilityTxt + "/" + priorityTxt, "syslogd");
        bldr.setCreationTime(message.getDate());

        // Set event host
        bldr.setHost(InetAddressUtils.getLocalHostName());

        final String hostAddress = message.getHostAddress();
        if (hostAddress != null && hostAddress.length() > 0) {
            // Set nodeId
            long nodeId = SyslogdIPMgr.getNodeId(hostAddress);
            if (nodeId != -1) {
                bldr.setNodeid(nodeId);
            }

            bldr.setInterface(addr(hostAddress));
        }
        
        bldr.setLogDest("logndisplay");


        // We will also here find out if, the host needs to
        // be replaced, the message matched to a UEI, and
        // last if we need to actually hide the message.
        // this being potentially helpful in avoiding showing
        // operator a password or other data that should be
        // confidential.

        /*
        * We matched on a regexp for host/message pair.
        * This can be a forwarded message as in BSD Style
        * or syslog-ng.
        * We assume that the host is given to us
        * as an IP/Hostname and that the resolver
        * on the ONMS host actually can resolve the
        * node to match against nodeId.
         */

        Pattern msgPat = null;
        Matcher msgMat = null;

        // Time to verify UEI matching.

        final String fullText = message.getFullText();
        final String matchedText = message.getMatchedMessage();

        final List<UeiMatch> ueiMatch = ueiList == null? null : ueiList.getUeiMatchCollection();
        if (ueiMatch == null) {
            LOG.warn("No ueiList configured.");
        } else {
            for (final UeiMatch uei : ueiMatch) {
                final boolean otherStuffMatches = matchFacility(uei.getFacilityCollection(), facilityTxt) &&
                                                  matchSeverity(uei.getSeverityCollection(), priorityTxt) &&
                                                  matchProcess(uei.getProcessMatch(), message.getProcessName()) && 
                                                  matchHostname(uei.getHostnameMatch(), message.getHostName()) &&
                                                  matchHostAddr(uei.getHostaddrMatch(), message.getHostAddress());
                
                if (otherStuffMatches && uei.getMatch().getType().equals("substr")) {
                    if (matchSubstring(discardUei, bldr, matchedText, uei)) {
                        break;
                    }
                } else if (otherStuffMatches && (uei.getMatch().getType().startsWith("regex"))) {
                    if (matchRegex(message, uei, bldr, discardUei)) {
                        break;
                    }
                }
            }
        }

        // Time to verify if we need to hide the message
        boolean doHide = false;
        final List<HideMatch> hideMatch = hideMessage == null? null : hideMessage.getHideMatchCollection();
        if (hideMatch == null) {
            LOG.warn("No hideMessage configured.");
        } else {
            for (final HideMatch hide : hideMatch) {
                if (hide.getMatch().getType().equals("substr")) {
                    if (fullText.contains(hide.getMatch().getExpression())) {
                        // We should hide the message based on this match
                    	doHide = true;
                    }            	
                } else if (hide.getMatch().getType().equals("regex")) {
                	try {
                    	msgPat = Pattern.compile(hide.getMatch().getExpression(), Pattern.MULTILINE);
                    	msgMat = msgPat.matcher(fullText);            		
                	} catch (PatternSyntaxException pse) {
			    LOG.warn("Failed to compile regex pattern '{}'", hide.getMatch().getExpression(), pse);
                		msgMat = null;
                	}
                	if ((msgMat != null) && (msgMat.find())) {
                        // We should hide the message based on this match
                		doHide = true;
                	}
                }
                if (doHide) {
                    LOG.debug("Hiding syslog message from Event - May contain sensitive data");
                    message.setMessage(HIDDEN_MESSAGE);
    	            // We want to stop here, no point in checking further hideMatches
    	            break;
                }
            }
        }

        // Using parms provides configurability.
        bldr.setLogMessage(message.getMessage());

        bldr.addParam("syslogmessage", message.getMessage());
        bldr.addParam("severity", "" + priorityTxt);
        bldr.addParam("timestamp", message.getSyslogFormattedDate());
        
        if (message.getProcessName() != null) {
            bldr.addParam("process", message.getProcessName());
        }

        bldr.addParam("service", "" + facilityTxt);

        if (message.getProcessId() != null) {
            bldr.addParam("processid", message.getProcessId().toString());
        }

        e.m_event = bldr.getEvent();
        return e;
    }

    private static boolean matchFind(final String expression, final String input, final String context) {
        final Pattern pat = getPattern(expression);
        if (pat == null) {
            LOG.debug("Unable to get pattern for expression '{}' in {} context", expression, context);
            return false;
        }
        final Matcher mat = pat.matcher(input);
        if (mat != null && mat.find()) return true;
        return false;
    }
    
    private static boolean matchHostAddr(final HostaddrMatch hostaddrMatch, final String hostAddress) {
        if (hostaddrMatch == null) return true;
        if (hostAddress == null) return false;
        
        final String expression = hostaddrMatch.getExpression();
        
        if (matchFind(expression, hostAddress, "hostaddr-match")) {
            LOG.trace("Successful regex hostaddr-match for input '{}' against expression '{}'", hostAddress, expression);
            return true;
        }
        return false;
    }
    
    private static boolean matchHostname(final HostnameMatch hostnameMatch, final String hostName) {
        if (hostnameMatch == null) return true;
        if (hostName == null) return false;
        
        final String expression = hostnameMatch.getExpression();
        
        if (matchFind(expression, hostName, "hostname-match")) {
            LOG.trace("Successful regex hostname-match for input '{}' against expression '{}'", hostName, expression);
            return true;
        }
        return false;
    }

    private static boolean matchProcess(final ProcessMatch processMatch, final String processName) {
        if (processMatch == null) return true;
        if (processName == null) return false;

        final String expression = processMatch.getExpression();

        if (matchFind(expression, processName, "process-match")) {
            LOG.trace("Successful regex process-match for input '{}' against expression '{}'", processName, expression);
            return true;
        }
        return false;
    }

    private static boolean matchSeverity(List<String> severities, String priorityTxt) {
        if (severities.size() == 0) return true;
        for (String severity : severities) {
            if (severity.toLowerCase().equals(priorityTxt.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean matchFacility(List<String> facilities, String facilityTxt) {
        if (facilities.size() == 0) return true;
        for (String facility : facilities) {
            if (facility.toLowerCase().equals(facilityTxt.toLowerCase())) return true;
        }
        return false;
    }

    private static Pattern getPattern(final String expression) {
        final Pattern msgPat = m_patterns.get(expression);
        if (msgPat == null) {
            try {
                final Pattern newPat = Pattern.compile(expression, Pattern.MULTILINE);
                m_patterns.put(expression, newPat);
                return newPat;
            } catch(final PatternSyntaxException pse) {
                LOG.warn("Failed to compile regex pattern '{}'", expression, pse);
        	}
        }
        return msgPat;
    }

    private static boolean matchSubstring(final String discardUei, final EventBuilder bldr, String message, final UeiMatch uei) throws MessageDiscardedException {
        boolean doIMatch = false;
        boolean traceEnabled = LOG.isTraceEnabled();
        if (message.contains(uei.getMatch().getExpression())) {
            if (discardUei.equals(uei.getUei())) {
                if (traceEnabled) LOG.trace("Specified UEI '{}' is same as discard-uei, discarding this message.", uei.getUei());
                throw new MessageDiscardedException();
            } else {
                //We can pass a new UEI on this
		    if (traceEnabled) LOG.trace("Changed the UEI of a Syslogd event, based on substring match, to : {}", uei.getUei());
                bldr.setUei(uei.getUei());
                // I think we want to stop processing here so the first
                // ueiMatch wins, right?
                doIMatch = true;
            }
        } else {
            if (traceEnabled) LOG.trace("No substring match for text of a Syslogd event to : {}", uei.getMatch().getExpression());
        }
        return doIMatch;
    }

    private static boolean matchRegex(final SyslogMessage message, final UeiMatch uei, final EventBuilder bldr, final String discardUei) throws MessageDiscardedException {
        boolean traceEnabled = LOG.isTraceEnabled();
        final String expression = uei.getMatch().getExpression();
        final Pattern msgPat = getPattern(expression);
        final Matcher msgMat;
        if (msgPat == null) {
            LOG.debug("Unable to create pattern for expression '{}'", expression);
            return false;
        } else {
            final String text;
            if (message.getMatchedMessage() != null) {
                text = message.getMatchedMessage();
            } else {
                text = message.getFullText();
            }
            msgMat = msgPat.matcher(text);
        }
        if ((msgMat != null) && (msgMat.find())) {
            if (discardUei.equals(uei.getUei())) {
                LOG.debug("Specified UEI '{}' is same as discard-uei, discarding this message.", uei.getUei());
                throw new MessageDiscardedException();
            }

            // We matched a UEI
            bldr.setUei(uei.getUei());
            if (msgMat.groupCount() > 0 && uei.getMatch().isDefaultParameterMapping()) {
                if (traceEnabled) LOG.trace("Doing default parameter mappings for this regex match.");
                for (int groupNum = 1; groupNum <= msgMat.groupCount(); groupNum++) {
                    if (traceEnabled) LOG.trace("Added parm 'group{}' with value '{}' to Syslogd event based on regex match group", groupNum, msgMat.group(groupNum));
                    bldr.addParam("group"+groupNum, msgMat.group(groupNum));
                }
            }
            if (msgMat.groupCount() > 0 && uei.getParameterAssignmentCount() > 0) {
                if (traceEnabled) LOG.trace("Doing user-specified parameter assignments for this regex match.");
                for (ParameterAssignment assignment : uei.getParameterAssignmentCollection()) {
                    String parmName = assignment.getParameterName();
                    String parmValue = msgMat.group(assignment.getMatchingGroup());
                    parmValue = parmValue == null ? "" : parmValue;
                    bldr.addParam(parmName, parmValue);
                    if (traceEnabled) LOG.trace("Added parm '{}' with value '{}' to Syslogd event based on user-specified parameter assignment", parmName, parmValue);
                }
            }
            // I think we want to stop processing here so the first
            // ueiMatch wins, right?
            return true;
        }
        if (traceEnabled) LOG.trace("Message '{}' did not regex-match pattern '{}'", message.getMessage(), expression);
        return false;
    }

    /**
     * Adds the event to the list of events acknowledged in this event XML
     * document.
     *
     * @param e The event to acknowledge.
     */
    void ackEvent(final Event e) {
        if (!m_ackEvents.contains(e))
            m_ackEvents.add(e);
    }

    /**
     * Returns the raw XML data as a string.
     */
    String getXmlData() {
        return m_eventXML;
    }

    /**
     * Returns the sender's address.
     */
    InetAddress getSender() {
        return m_sender;
    }

    /**
     * Returns the sender's port
     */
    int getPort() {
        return m_port;
    }

    /**
     * Get the acknowledged events
     *
     * @return a {@link java.util.List} object.
     */
    public List<Event> getAckedEvents() {
        return m_ackEvents;
    }

    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        return m_event;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the instance matches the object based upon the remote
     * agent's address &amp; port. If the passed instance is from the same
     * agent then it is considered equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (o != null && o instanceof ConvertToEvent) {
            final ConvertToEvent e = (ConvertToEvent) o;
            return (this == e || (m_port == e.m_port && m_sender.equals(e.m_sender)));
        }
        return false;
    }

    /**
     * Returns the hash code of the instance. The hash code is computed by
     * taking the bitwise XOR of the port and the agent's Internet address
     * hash code.
     *
     * @return The 32-bit has code for the instance.
     */
    @Override
    public int hashCode() {
        return (m_port ^ m_sender.hashCode());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("Sender", m_sender)
            .append("Port", m_port)
            .append("Acknowledged Events", m_ackEvents)
            .append("Event", m_event)
            .toString();
    }
}

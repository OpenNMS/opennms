/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.HostaddrMatch;
import org.opennms.netmgt.config.syslogd.HostnameMatch;
import org.opennms.netmgt.config.syslogd.ParameterAssignment;
import org.opennms.netmgt.config.syslogd.ProcessMatch;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This routine does the majority of Syslogd's work.
 * Improvements are most likely to be made.
 * 
 * TODO: This class is sloooow. It needs to be sped up significantly
 * to handle increased syslog volume.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class ConvertToEvent {

    private static final Logger LOG = LoggerFactory.getLogger(ConvertToEvent.class);

    /** Constant <code>HIDDEN_MESSAGE="The message logged has been removed due"{trunked}</code> */
    protected static final String HIDDEN_MESSAGE = "The message logged has been removed due to configuration of Syslogd; it may contain sensitive data.";

    private final Event m_event;

    private static final LoadingCache<String,Pattern> CACHED_PATTERNS = CacheBuilder.newBuilder().build(
        new CacheLoader<String,Pattern>() {
            public Pattern load(String expression) {
                try {
                    return Pattern.compile(expression, Pattern.MULTILINE);
                } catch(final PatternSyntaxException e) {
                    LOG.warn("Failed to compile regex pattern '{}'", expression, e);
                    return null;
                }
            }
        }
    );

    /**
     * Constructs a new event encapsulation instance based upon the
     * information passed to the method. The passed datagram data is decoded
     * into a string using the <tt>US-ASCII</tt> character encoding.
     *
     * @param packet The datagram received from the remote agent.
     * @throws MessageDiscardedException 
     */
    public ConvertToEvent(
        final String systemId,
        final String location,
        final DatagramPacket packet,
        final SyslogdConfig config
    ) throws MessageDiscardedException {
        this(systemId, location, packet.getAddress(), packet.getPort(), new String(packet.getData(), 0, packet.getLength(), StandardCharsets.US_ASCII), config);
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
     * @throws MessageDiscardedException 
     */
    public ConvertToEvent(
        final String systemId,
        final String location,
        final InetAddress addr,
        final int port,
        final String data,
        final SyslogdConfig config
    ) throws MessageDiscardedException {

        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        String syslogString = data;
        // Trim trailing nulls from the string
        while (syslogString.endsWith("\0")) {
            syslogString = syslogString.substring(0, syslogString.length() - 1);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Converting to event: {}", this);
        }

        SyslogParser parser = SyslogParser.getParserInstance(config, syslogString);
        if (!parser.find()) {
            throw new MessageDiscardedException(String.format("Message does not match regex: '%s'", syslogString));
        }
        SyslogMessage message;
        try {
            message = parser.parse();
        } catch (final SyslogParserException ex) {
            LOG.debug("Unable to parse '{}'", syslogString, ex);
            throw new MessageDiscardedException(ex);
        }

        if (message == null) {
            throw new MessageDiscardedException(String.format("Unable to parse message: '%s'", syslogString));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("got syslog message {}", message);
        }

        // Build a basic event out of the syslog message
        final String priorityTxt = message.getSeverity().toString();
        final String facilityTxt = message.getFacility().toString();

        EventBuilder bldr = new EventBuilder("uei.opennms.org/syslogd/" + facilityTxt + "/" + priorityTxt, "syslogd");


        // Set constant values in EventBuilder

        // Set monitoring system
        bldr.setDistPoller(systemId);
        // Set event host
        bldr.setHost(InetAddressUtils.getLocalHostName());
        // Set default event destination to logndisplay
        bldr.setLogDest("logndisplay");


        // Set values from SyslogMessage in the EventBuilder

        final InetAddress hostAddress = message.getHostAddress();
        if (hostAddress != null) {
            // Set nodeId
            InterfaceToNodeCache cache = AbstractInterfaceToNodeCache.getInstance();
            if (cache != null) {
                int nodeId = cache.getNodeId(location, hostAddress);
                if (nodeId > 0) {
                    bldr.setNodeid(nodeId);
                }
            }

            bldr.setInterface(hostAddress);
        }

        bldr.setTime(message.getDate());

        bldr.setLogMessage(message.getMessage());
        // Using parms provides configurability.
        bldr.addParam("syslogmessage", message.getMessage());

        bldr.addParam("severity", "" + priorityTxt);

        bldr.addParam("timestamp", message.getRfc3164FormattedDate());

        if (message.getProcessName() != null) {
            bldr.addParam("process", message.getProcessName());
        }

        bldr.addParam("service", "" + facilityTxt);

        if (message.getProcessId() != null) {
            bldr.addParam("processid", message.getProcessId().toString());
        }

        // Post-process the message based on the SyslogdConfig

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

        // Time to verify UEI matching.

        final List<UeiMatch> ueiMatch = (config.getUeiList() == null ? Collections.emptyList() : config.getUeiList().getUeiMatchCollection());
        for (final UeiMatch uei : ueiMatch) {
            final boolean messageMatchesUeiListEntry = containsIgnoreCase(uei.getFacilityCollection(), facilityTxt) &&
                                              containsIgnoreCase(uei.getSeverityCollection(), priorityTxt) &&
                                              matchProcess(uei.getProcessMatch(), message.getProcessName()) &&
                                              matchHostname(uei.getHostnameMatch(), message.getHostName()) &&
                                              matchHostAddr(uei.getHostaddrMatch(), str(hostAddress));

            if (messageMatchesUeiListEntry) {
                if (uei.getMatch().getType().equals("substr")) {
                    if (matchSubstring(message.getMessage(), uei, bldr, config.getDiscardUei())) {
                        break;
                    }
                } else if ((uei.getMatch().getType().startsWith("regex"))) {
                    if (matchRegex(message.getMessage(), uei, bldr, config.getDiscardUei())) {
                        break;
                    }
                }
            }
        }

        final String fullText = message.asRfc3164Message();

        // Time to verify if we need to hide the message
        final List<HideMatch> hideMatch = (config.getHideMessages() == null ? Collections.emptyList() : config.getHideMessages().getHideMatchCollection());
        boolean doHide = false;
        for (final HideMatch hide : hideMatch) {
            if (hide.getMatch().getType().equals("substr")) {
                if (fullText.contains(hide.getMatch().getExpression())) {
                    // We should hide the message based on this match
                    doHide = true;
                    break;
                }
            } else if (hide.getMatch().getType().equals("regex")) {
                try {
                    Pattern msgPat = getPattern(hide.getMatch().getExpression());
                    Matcher msgMat = msgPat.matcher(fullText);
                    if (msgMat.find()) {
                        // We should hide the message based on this match
                        doHide = true;
                        break;
                    }
                } catch (PatternSyntaxException pse) {
                    LOG.warn("Failed to compile hide-match regex pattern '{}'", hide.getMatch().getExpression(), pse);
                }
            }
        }

        if (doHide) {
            LOG.debug("Hiding syslog message from Event - May contain sensitive data");
            bldr.setLogMessage(HIDDEN_MESSAGE);
            bldr.setParam("syslogmessage", HIDDEN_MESSAGE);
        }

        m_event = bldr.getEvent();
    }

    private static boolean matchFind(final String expression, final String input, final String context) {
        if (input == null) {
            return false;
        }
        final Pattern pat = getPattern(expression);
        if (pat == null) {
            LOG.debug("Unable to get pattern for expression '{}' in {} context", expression, context);
            return false;
        }
        final Matcher mat = pat.matcher(input);
        if (mat != null && mat.find()) {
            LOG.trace("Successful regex {} for input '{}' against expression '{}'", context, input, expression);
            return true;
        } else {
           return false;
        }
    }

    private static boolean matchHostAddr(final HostaddrMatch hostaddrMatch, final String hostAddress) {
        if (hostaddrMatch == null) return true;
        return matchFind(hostaddrMatch.getExpression(), hostAddress, "hostaddr-match");
    }

    private static boolean matchHostname(final HostnameMatch hostnameMatch, final String hostName) {
        if (hostnameMatch == null) return true;
        return matchFind(hostnameMatch.getExpression(), hostName, "hostname-match");
    }

    private static boolean matchProcess(final ProcessMatch processMatch, final String processName) {
        if (processMatch == null) return true;
        return matchFind(processMatch.getExpression(), processName, "process-match");
    }

    private static boolean containsIgnoreCase(List<String> collection, String match) {
         if (collection.size() == 0) return true;
         for (String string : collection) {
             if (string.equalsIgnoreCase(match)) return true;
         }
         return false;
    }

    private static Pattern getPattern(final String expression) {
        return CACHED_PATTERNS.getUnchecked(expression);
    }

    /**
     * Checks the message for substring matches to a {@link UeiMatch}. If the message
     * matches, then the UEI is updated (or the event is discarded if the discard
     * UEI is used). Parameter assignments are NOT performed for substring matches.
     * 
     * @param message
     * @param uei
     * @param bldr
     * @param discardUei
     * @return
     * @throws MessageDiscardedException
     */
    private static boolean matchSubstring(String message, final UeiMatch uei, final EventBuilder bldr, final String discardUei) throws MessageDiscardedException {
        final boolean traceEnabled = LOG.isTraceEnabled();
        if (message.contains(uei.getMatch().getExpression())) {
            if (discardUei.equals(uei.getUei())) {
                if (traceEnabled) LOG.trace("Specified UEI '{}' is same as discard-uei, discarding this message.", uei.getUei());
                throw new MessageDiscardedException();
            } else {
                // Update the UEI to the new value
                if (traceEnabled) LOG.trace("Changed the UEI of a Syslogd event, based on substring match, to : {}", uei.getUei());
                bldr.setUei(uei.getUei());
                return true;
            }
        } else {
            if (traceEnabled) LOG.trace("No substring match for text of a Syslogd event to : {}", uei.getMatch().getExpression());
            return false;
        }
    }

    /**
     * Checks the message for matches to a {@link UeiMatch}. If the message
     * matches, then the UEI is updated (or the event is discarded if the discard
     * UEI is used) and parameters are added to the event.
     * 
     * @param message
     * @param uei
     * @param bldr
     * @param discardUei
     * @return
     * @throws MessageDiscardedException
     */
    private static boolean matchRegex(final String message, final UeiMatch uei, final EventBuilder bldr, final String discardUei) throws MessageDiscardedException {
        final boolean traceEnabled = LOG.isTraceEnabled();
        final String expression = uei.getMatch().getExpression();
        final Pattern msgPat = getPattern(expression);
        if (msgPat == null) {
            LOG.debug("Unable to create pattern for expression '{}'", expression);
            return false;
        } 

        final Matcher msgMat = msgPat.matcher(message);

        // If the message matches the regex
        if ((msgMat != null) && (msgMat.find())) {
            // Discard the message if the UEI is set to the discard UEI
            if (discardUei.equals(uei.getUei())) {
                if (traceEnabled) LOG.trace("Specified UEI '{}' is same as discard-uei, discarding this message.", uei.getUei());
                throw new MessageDiscardedException();
            } else {
                // Update the UEI to the new value
                if (traceEnabled) LOG.trace("Changed the UEI of a Syslogd event, based on regex match, to : {}", uei.getUei());
                bldr.setUei(uei.getUei());
            }

            if (msgMat.groupCount() > 0) {
                // Perform default parameter mapping
                if (uei.getMatch().isDefaultParameterMapping()) {
                    if (traceEnabled) LOG.trace("Doing default parameter mappings for this regex match.");
                    for (int groupNum = 1; groupNum <= msgMat.groupCount(); groupNum++) {
                        if (traceEnabled) LOG.trace("Added parm 'group{}' with value '{}' to Syslogd event based on regex match group", groupNum, msgMat.group(groupNum));
                        bldr.addParam("group"+groupNum, msgMat.group(groupNum));
                    }
                }

                // If there are specific parameter mappings as well, perform those mappings
                if (uei.getParameterAssignmentCount() > 0) {
                    if (traceEnabled) LOG.trace("Doing user-specified parameter assignments for this regex match.");
                    for (ParameterAssignment assignment : uei.getParameterAssignmentCollection()) {
                        String parmName = assignment.getParameterName();
                        String parmValue = msgMat.group(assignment.getMatchingGroup());
                        parmValue = parmValue == null ? "" : parmValue;
                        bldr.addParam(parmName, parmValue);
                        if (traceEnabled) {
                            LOG.trace("Added parm '{}' with value '{}' to Syslogd event based on user-specified parameter assignment", parmName, parmValue);
                        }
                    }
                }
            }

            return true;
        }

        if (traceEnabled) LOG.trace("Message portion '{}' did not regex-match pattern '{}'", message, expression);
        return false;
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
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("Event", m_event)
            .toString();
    }
}

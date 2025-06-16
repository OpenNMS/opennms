/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomSyslogParser extends SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(CustomSyslogParser.class);
    private static final Pattern m_messageIdPattern = Pattern.compile("^((\\S+):\\s*)");
    // date pattern has been updated to support space at start and end of
    // the message due to which the date match failed and current system date
    // used to be passed which cause 3ms more time in parsing message
    private static final Pattern m_datePattern = Pattern.compile("^\\s*((\\d\\d\\d\\d-\\d\\d-\\d\\d)\\s*)");
    private static final Pattern m_oldDatePattern = Pattern.compile("^\\s*(\\S\\S\\S\\s+\\d{1,2}\\s+\\d\\d:\\d\\d:\\d\\d)\\s+");

    private final Pattern m_forwardingPattern;
    private final int m_matchingGroupHost;
    private final int m_matchingGroupMessage;

    public CustomSyslogParser(final SyslogdConfig config, final ByteBuffer text) throws SyslogParserException {
        super(config, text);

        if (config.getForwardingRegexp() == null) {
            throw new SyslogParserException("no forwarding regular expression defined");
        }
        final String forwardingRegexp = config.getForwardingRegexp();
        m_forwardingPattern = Pattern.compile(forwardingRegexp, Pattern.MULTILINE);
        m_matchingGroupHost = config.getMatchingGroupHost();
        m_matchingGroupMessage = config.getMatchingGroupMessage();
    }

    @Override
    protected SyslogMessage parse() throws SyslogParserException {
        LOG.debug("Message parse start");
        final SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setParserClass(getClass());

        String message = SyslogParser.fromByteBuffer(getText());

        int lbIdx = message.indexOf('<');
        int rbIdx = message.indexOf('>');

        if (lbIdx < 0 || rbIdx < 0 || lbIdx >= (rbIdx - 1)) {
            LOG.warn("Syslogd received an unparsable message!");
            return null;
        }

        int priCode = 0;
        String priStr = message.substring(lbIdx + 1, rbIdx);

        priCode = parseInt(priStr, "ERROR Bad priority code '{}'");

        LOG.trace("priority code = {}", priCode);

        syslogMessage.setFacility(SyslogFacility.getFacilityForCode(priCode));
        syslogMessage.setSeverity(SyslogSeverity.getSeverityForCode(priCode));

        message = message.substring(rbIdx + 1, message.length());

        final Matcher idMatcher = m_messageIdPattern.matcher(message);
        if (idMatcher.find()) {
            final String messageId = idMatcher.group(2);
            LOG.trace("found message ID '{}'", messageId);
            syslogMessage.setMessageID(messageId);
            message = message.substring(idMatcher.group(1).length() - 1);
        }

        LOG.trace("message = {}", message);

        String timestamp;
        Matcher oldDateMatcher = m_oldDatePattern.matcher(message);
        if (oldDateMatcher.find()) {
            LOG.trace("stdMsg = {}", "true");
            timestamp = oldDateMatcher.group(1);
            message = oldDateMatcher.replaceFirst("");
        } else {
            final Matcher stampMatcher = m_datePattern.matcher(message);
            if (stampMatcher.find()) {
                LOG.trace("stdMsg = {}", "false");
                timestamp = stampMatcher.group(2);
                LOG.trace("found timestamp '{}'", timestamp);
                // message = message.substring(stampMatcher.group(1).length());
            } else {
                try {
                    timestamp = SyslogTimeStamp.getInstance().format(new Date());
                } catch (final IllegalArgumentException ex) {
                    LOG.debug("ERROR INTERNAL DATE ERROR!");
                    timestamp = "";
                }
            }
        }

        LOG.trace("timestamp = {}", timestamp);
        syslogMessage.setDate(parseDate(timestamp));

        // These 2 debugs will aid in analyzing the regexes as syslog seems
        // to differ a lot depending on implementation or message structure.

        LOG.trace("message = {}", message);
        LOG.trace("pattern = {}", m_forwardingPattern);
        LOG.trace("host group = {}", m_matchingGroupHost);
        LOG.trace("message group = {}", m_matchingGroupMessage);

        // We will also here find out if, the host needs to
        // be replaced, the message matched to a UEI, and
        // last if we need to actually hide the message.
        // this being potentially helpful in avoiding showing
        // operator a password or other data that should be
        // confidential.

        final Pattern pattern = m_forwardingPattern;
        final Matcher m = pattern.matcher(message);

        /*
         * We matched on a regexp for host/message pair.
         * This can be a forwarded message as in BSD Style
         * or syslog-ng.
         */

        if (m.matches()) {

            final String matchedMessage = m.group(m_matchingGroupMessage);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Syslog message '{}' matched regexp '{}'", message, m_forwardingPattern);
                LOG.trace("Found host '{}'", m.group(m_matchingGroupHost));
                LOG.trace("Found message '{}'", matchedMessage);
            }

            syslogMessage.setHostName(m.group(m_matchingGroupHost));

            message = matchedMessage;
        } else {
            LOG.debug("Regexp not matched: {}", message);
            return null;
        }

        lbIdx = message.indexOf('[');
        rbIdx = message.indexOf(']');
        final int colonIdx = message.indexOf(':');
        final int spaceIdx = message.indexOf(' ');

        String processId = null;
        String processName = null;

        // If statement has been reversed in order to make the decision faster
        // rather than always calculating lbIdx < (rbIdx - 1) which might fail

        if (lbIdx < (rbIdx - 1) && colonIdx == (rbIdx + 1) && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, lbIdx);
            processId = message.substring(lbIdx + 1, rbIdx);
            message = message.substring(colonIdx + 2);
        } else if (colonIdx > 0 && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, colonIdx);
            message = message.substring(colonIdx + 2);
        }

        if (processId != null) {
            syslogMessage.setProcessId(processId);
        }
        if (processName != null) {
            syslogMessage.setProcessName(processName);
        }
        syslogMessage.setMessage(message.trim());

        LOG.debug("Message parse end");
        return syslogMessage;
    }

    private static int parseInt(String stringToInt, String logMessage) {
        int integerValue = 0;
        try {
            integerValue = Integer.parseInt(stringToInt);
        } catch (final NumberFormatException e) {
            LOG.debug(logMessage, stringToInt);
        }
        return integerValue;
    }
}

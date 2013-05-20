/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;

public class CustomSyslogParser extends SyslogParser {
    private static final Pattern m_messageIdPattern = Pattern.compile("^((\\S+):\\s*)");
    private static final Pattern m_datePattern = Pattern.compile("^((\\d\\d\\d\\d-\\d\\d-\\d\\d)\\s+)");
    private static final Pattern m_oldDatePattern = Pattern.compile("^\\s*(\\S\\S\\S\\s+\\d{1,2}\\s+\\d\\d:\\d\\d:\\d\\d)\\s+");

    private Pattern m_forwardingPattern;
    private int m_matchingGroupHost;
    private int m_matchingGroupMessage;

    protected CustomSyslogParser(final String text) throws SyslogParserException {
        super(text);

        final SyslogdConfig config = SyslogdConfigFactory.getInstance();
        final String forwardingRegexp = config.getForwardingRegexp();
        if (forwardingRegexp == null || forwardingRegexp.length() == 0) {
            throw new SyslogParserException("no forwarding regular expression defined");
        }
        m_forwardingPattern = Pattern.compile(forwardingRegexp, Pattern.MULTILINE);
        m_matchingGroupHost = config.getMatchingGroupHost();
        m_matchingGroupMessage = config.getMatchingGroupMessage();
    }

    public static SyslogParser getParser(final String text) throws SyslogParserException {
        return new CustomSyslogParser(text);
    }

    @Override
    public SyslogMessage parse() throws SyslogParserException {
        final SyslogMessage syslogMessage = new SyslogMessage();

        String message = getText();

        int lbIdx = message.indexOf('<');
        int rbIdx = message.indexOf('>');

        if (lbIdx < 0 || rbIdx < 0 || lbIdx >= (rbIdx - 1)) {
            LogUtils.warnf(this, "Syslogd received an unparsable message!");
        }

        int priCode = 0;
        String priStr = message.substring(lbIdx + 1, rbIdx);

        try {
            priCode = Integer.parseInt(priStr);
        } catch (final NumberFormatException ex) {
            LogUtils.debugf(this, "ERROR Bad priority code '%s'", priStr);

        }

        LogUtils.tracef(this, "priority code = %d", priCode);

        syslogMessage.setFacility(SyslogFacility.getFacilityForCode(priCode));
        syslogMessage.setSeverity(SyslogSeverity.getSeverityForCode(priCode));

        message = message.substring(rbIdx + 1, message.length());

        final Matcher idMatcher = m_messageIdPattern.matcher(message);
        if (idMatcher.find()) {
            final String messageId = idMatcher.group(2);
            LogUtils.tracef(this, "found message ID '%s'", messageId);
            syslogMessage.setMessageID(messageId);
            message = message.substring(idMatcher.group(1).length() - 1);
        }

        LogUtils.tracef(this, "message = %s", message);
        
        Matcher oldDateMatcher = m_oldDatePattern.matcher(message);
        if (!oldDateMatcher.find()) {
            oldDateMatcher = null;
        }
        LogUtils.tracef(this, "stdMsg = %s", Boolean.toString(oldDateMatcher != null));
        
        if (!this.find()) {
            if (traceEnabled()) {
                LogUtils.tracef(this, "Lenient Syslog pattern '%s' did not match '%s'", getPattern(), getText());
            }
            return null;
        }

        String timestamp;

        if (oldDateMatcher == null) {
            final Matcher stampMatcher = m_datePattern.matcher(message);
            if (stampMatcher.find()) {
                timestamp = stampMatcher.group(2);
                LogUtils.tracef(this, "found timestamp '%s'", timestamp);
//                message = message.substring(stampMatcher.group(1).length());
            } else {
                try {
                    timestamp = SyslogTimeStamp.getInstance().format(new Date());
                } catch (final IllegalArgumentException ex) {
                    LogUtils.debugf(this, "ERROR INTERNAL DATE ERROR!");
                    timestamp = "";
                }
            }
        } else {
            timestamp = oldDateMatcher.group(1);
            message = oldDateMatcher.replaceFirst("");
        }

        LogUtils.tracef(this, "timestamp = %s", timestamp);
        syslogMessage.setDate(parseDate(timestamp));
        
        // These 2 debugs will aid in analyzing the regexes as syslog seems
        // to differ a lot depending on implementation or message structure.

        if (LogUtils.isTraceEnabled(this)) {
            LogUtils.tracef(this, "message = %s", message);
            LogUtils.tracef(this, "pattern = %s", m_forwardingPattern);
            LogUtils.tracef(this, "host group = %d", m_matchingGroupHost);
            LogUtils.tracef(this, "message group = %d", m_matchingGroupMessage);
        }

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
            syslogMessage.setMatchedMessage(matchedMessage);

            if (LogUtils.isTraceEnabled(this)) {
                LogUtils.tracef(this, "Syslog message '%s' matched regexp '%s'", message, m_forwardingPattern);
                LogUtils.tracef(this, "Found host '%s'", m.group(m_matchingGroupHost));
                LogUtils.tracef(this, "Found message '%s'", matchedMessage);
            }

            syslogMessage.setHostName(m.group(m_matchingGroupHost));
            
            message = matchedMessage;
        } else {
            LogUtils.debugf(this, "Regexp not matched: %s", message);            
            return null;
        }

        lbIdx = message.indexOf('[');
        rbIdx = message.indexOf(']');
        final int colonIdx = message.indexOf(':');
        final int spaceIdx = message.indexOf(' ');

        int processId = 0;
        String processName = "";
        String processIdStr = "";

        if (lbIdx < (rbIdx - 1) && colonIdx == (rbIdx + 1) && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, lbIdx);
            processIdStr = message.substring(lbIdx + 1, rbIdx);
            message = message.substring(colonIdx + 2);

            try {
                processId = Integer.parseInt(processIdStr);
            } catch (final NumberFormatException ex) {
                LogUtils.debugf(this, "Bad process id '%s'", processIdStr);
                processId = 0;
            }
        } else if (lbIdx < 0 && rbIdx < 0 && colonIdx > 0 && spaceIdx == (colonIdx + 1)) {
            processName = message.substring(0, colonIdx);
            message = message.substring(colonIdx + 2);
        }

        syslogMessage.setProcessId(processId);
        syslogMessage.setProcessName(processName);
        syslogMessage.setMessage(message.trim());

        return syslogMessage;
    }
}

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.SyslogdConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser that handles the new Syslog standard (as defined in RFC5424).
 * It has strict parsing that should match the grammar specified in the RFC, 
 * although it currently discards structured data. Like the SyslogNGParser, 
 * it ignores forwarding-regexp, matching-group-host, and matching-group-message 
 * in favor of stricter parsing of the known grammer.
 */
public class Rfc5424SyslogParser extends SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(Rfc5424SyslogParser.class);

    //                                                                <PRI>VERSION             TIMESTAMP   HOST   APP    PROC     MSGID  STRUCTURED           BOM     MSG
    private static final Pattern m_rfc5424Pattern = Pattern.compile("^<(\\d{1,3})>(\\d{0,2}?) (\\S+T\\S+) (\\S*) (\\S*) (\\d+|-) (\\S*) ((?:\\[.*?\\])*|-)(?: \uFEFF?(.*?))?$", Pattern.MULTILINE);

    private static final Pattern m_dateWithOffset = Pattern.compile("^(.*[\\-\\+]\\d\\d):?(\\d\\d)$");

    public Rfc5424SyslogParser(final SyslogdConfig config, final ByteBuffer text) {
        super(config, text);
    }

    @Override
    protected Pattern getPattern() {
        return m_rfc5424Pattern;
    }

    @Override
    protected SyslogMessage parse() throws SyslogParserException {
        if (!this.find()) {
            if (traceEnabled()) {
                LOG.trace("'{}' did not match '{}'", m_rfc5424Pattern, getText());
            }
            return null;
        }

        final Matcher matcher = getMatcher();
        final SyslogMessage message = new SyslogMessage();
        message.setParserClass(getClass());
        try {
            final int priorityField = Integer.parseInt(matcher.group(1));
            message.setFacility(SyslogFacility.getFacilityForCode(priorityField));
            message.setSeverity(SyslogSeverity.getSeverityForCode(priorityField));
        } catch (final NumberFormatException e) {
            LOG.debug("Unable to parse priority field '{}'", matcher.group(1), e);
        }
        if (matcher.group(2).length() != 0) {
            try {
                int version = Integer.parseInt(matcher.group(2));
                message.setVersion(version);
            } catch (NumberFormatException e) {
                LOG.debug("Unable to parse version ({}) as a number.", matcher.group(2), e);
            }
        }
        if (!matcher.group(3).equals("-")) {
            message.setDate(parseDate(matcher.group(3)));
        }
        if (!matcher.group(4).equals("-")) {
            message.setHostName(matcher.group(4));
        }
        if (!matcher.group(5).equals("-")) {
            message.setProcessName(matcher.group(5));
        }
        if (!matcher.group(6).equals("-")) {
            message.setProcessId(matcher.group(6));
        }
        if (!matcher.group(7).equals("-")) {
            message.setMessageID(matcher.group(7));
        }
        final String messageText = matcher.group(9);
        if (messageText != null && messageText.length() != 0) {
            message.setMessage(messageText.trim());
        }
        return message;
    }

    protected Date parseDate(final String dateString) {
        if (dateString.endsWith("Z")) {
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                adjustTimeZone(df);
                return df.parse(dateString);
            } catch (final Exception e) {
                // try again with optional decimals
                try {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ROOT);
                    df.setLenient(true);
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    adjustTimeZone(df);
                    return df.parse(dateString);
                } catch (final Exception pe) {
                    LOG.debug("Unable to parse date string '{}'.", dateString, pe);
                }
            }
        } else {
            final Matcher matcher = m_dateWithOffset.matcher(dateString);
            final String newString;
            if (matcher.find()) {
                newString = matcher.group(1) + matcher.group(2);
            } else {
                final String first = dateString.substring(0, dateString.lastIndexOf('-'));
                final String last = dateString.substring(dateString.lastIndexOf('-'));
                newString = first + last.replace(":", "");
            }
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
                return df.parse(newString);
            } catch (final Exception e) {
                // try again with optional decimals
                try {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.ROOT);
                    df.setLenient(true);
                    return df.parse(newString);
                } catch (final Exception pe) {
                    LOG.debug("Unable to parse date string '{}'.", newString, pe);
                }
            }
        }
        return null;
    }
}

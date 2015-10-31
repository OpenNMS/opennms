/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

public class Rfc5424SyslogParser extends SyslogParser {
    private static final Logger LOG = LoggerFactory.getLogger(Rfc5424SyslogParser.class);

    //                                                                <PRI>VERSION            TIMESTAMP    HOST   APP    PROC     MSGID  STRUCTURED   MSG
    private static final Pattern m_rfc5424Pattern = Pattern.compile("^<(\\d{1,3})>(\\d{0,2}?) (\\S+T\\S+) (\\S*) (\\S*) (\\d+|-) (\\S*) ((?:\\[.*?\\])*|-)(?: (?:BOM)?(.*?))?$", Pattern.MULTILINE);

    private static final Pattern m_dateWithOffset = Pattern.compile("^(.*[\\-\\+]\\d\\d):?(\\d\\d)$");

    public Rfc5424SyslogParser(final SyslogdConfig config, final String text) {
        super(config, text);
    }

    @Override
    protected Pattern getPattern() {
        return m_rfc5424Pattern;
    }

    @Override
    public SyslogMessage parse() throws SyslogParserException {
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
            try {
                message.setProcessId(Integer.parseInt(matcher.group(6)));
            } catch (final NumberFormatException e) {
                LOG.debug("Unable to parse process ID '{}' as a number.", matcher.group(6), e);
            }
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

    protected static Date parseDate(final String dateString) {
        if (dateString.endsWith("Z")) {
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df.parse(dateString);
            } catch (final Exception e) {
                // try again with optional decimals
                try {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ROOT);
                    df.setLenient(true);
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
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

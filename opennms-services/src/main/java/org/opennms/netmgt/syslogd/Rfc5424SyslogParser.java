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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;

public class Rfc5424SyslogParser extends SyslogParser {
    //                                                                <PRI>VERSION            TIMESTAMP    HOST   APP    PROC     MSGID  STRUCTURED   MSG
    private static final Pattern m_rfc5424Pattern = Pattern.compile("^<(\\d{1,3})>(\\d{0,2}?) (\\S+T\\S+) (\\S*) (\\S*) (\\d+|-) (\\S*) ((?:\\[.*?\\])*|-)(?: (?:BOM)?(.*?))?$", Pattern.MULTILINE);

    private static final Pattern m_dateWithOffset = Pattern.compile("^(.*[\\-\\+]\\d\\d):?(\\d\\d)$");

    protected Rfc5424SyslogParser(final String text) {
        super(text);
    }

    public static SyslogParser getParser(final String text) {
        return new Rfc5424SyslogParser(text);
    }

    @Override
    protected Pattern getPattern() {
        return m_rfc5424Pattern;
    }

    @Override
    public SyslogMessage parse() throws SyslogParserException {
        if (!this.find()) {
            if (traceEnabled()) {
                LogUtils.tracef(this, "'%s' did not match '%s'", m_rfc5424Pattern, getText());
            }
            return null;
        }

        final Matcher matcher = getMatcher();
        final SyslogMessage message = new SyslogMessage();
        try {
            final int priorityField = Integer.parseInt(matcher.group(1));
            message.setFacility(SyslogFacility.getFacilityForCode(priorityField));
            message.setSeverity(SyslogSeverity.getSeverityForCode(priorityField));
        } catch (final NumberFormatException e) {
            LogUtils.debugf(this, e, "Unable to parse priority field '%s'", matcher.group(1));
        }
        if (matcher.group(2).length() != 0) {
            try {
                int version = Integer.parseInt(matcher.group(2));
                message.setVersion(version);
            } catch (NumberFormatException e) {
                LogUtils.debugf(this, e, "Unable to parse version (%s) as a number.", matcher.group(2));
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
                LogUtils.debugf(this, e, "Unable to parse process ID '%s' as a number.", matcher.group(6));
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

    @Override
    protected Date parseDate(final String dateString) {
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
                    LogUtils.debugf(this, pe, "Unable to parse date string '%s'.", dateString);
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
                    LogUtils.debugf(this, pe, "Unable to parse date string '%s'.", newString);
                }
            }
        }
        return null;
    }
}

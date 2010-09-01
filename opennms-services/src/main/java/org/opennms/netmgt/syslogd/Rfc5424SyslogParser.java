package org.opennms.netmgt.syslogd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;

public class Rfc5424SyslogParser extends SyslogParser {
    //                                                                <PRI>VERSION            TIMESTAMP    HOST   APP    PROC     MSGID  STRUCTURED   MSG
    private static final Pattern m_rfc5424Pattern = Pattern.compile("^<(\\d{1,3})>(\\d{0,2}?) (\\S+T\\S+) (\\S*) (\\S*) (\\d+|-) (\\S*) ((?:\\[.*?\\])*|-)(?: (?:BOM)?(.*?))?$", Pattern.MULTILINE);

    protected Rfc5424SyslogParser(final String text) {
        super(text);
    }

    public static SyslogParser getParser(final String text) {
        return new Rfc5424SyslogParser(text);
    }

    protected Pattern getPattern() {
        return m_rfc5424Pattern;
    }

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
            int priorityField = Integer.parseInt(matcher.group(1));
            message.setFacility(getFacility(priorityField));
            message.setSeverity(getSeverity(priorityField));
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
            message.setMessage(messageText);
        }
        return message;
    }

    private Date parseDate(final String dateString) {
        if (dateString.endsWith("Z")) {
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df.parse(dateString);
            } catch (final Exception e) {
                // try again with optional decimals
                try {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
                    df.setLenient(true);
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return df.parse(dateString);
                } catch (final Exception pe) {
                    LogUtils.debugf(this, pe, "Unable to parse date string '%s'.", dateString);
                }
            }
        } else {
            final String first = dateString.substring(0, dateString.lastIndexOf('-'));
            final String last = dateString.substring(dateString.lastIndexOf('-'));
            final String newString = first + last.replace(":", "");
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                return df.parse(newString);
            } catch (final Exception e) {
                // try again with optional decimals
                try {
                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
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

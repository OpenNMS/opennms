package org.opennms.netmgt.syslogd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;

public class CustomSyslogParser extends SyslogParser {
    private static final Pattern m_syslogPattern = Pattern.compile("^<(\\d{1,3})>(\\d{0,2}) ?(\\S+?):? (?:(\\d\\d\\d\\d-\\d\\d-\\d\\d) )?(?:(\\S+) )(?:(\\S+?)(?:\\[(\\d+)\\])?: ){0,1}(\\S.*?)$", Pattern.MULTILINE);
    private static Pattern m_forwardingPattern;
    private static int m_matchingGroupHost;
    private static int m_matchingGroupMessage;

    protected CustomSyslogParser(final String text) throws SyslogParserException {
        super(text);
        if (m_forwardingPattern == null) {
            final SyslogdConfig config = SyslogdConfigFactory.getInstance();
            final String forwardingRegexp = config.getForwardingRegexp();
            if (forwardingRegexp == null || forwardingRegexp.length() == 0) {
                throw new SyslogParserException("no forwarding regular expression defined");
            }
            m_forwardingPattern = Pattern.compile(forwardingRegexp, Pattern.MULTILINE);
            m_matchingGroupHost = config.getMatchingGroupHost();
            m_matchingGroupMessage = config.getMatchingGroupMessage();
        }
    }

    public static SyslogParser getParser(final String text) throws SyslogParserException {
        return new CustomSyslogParser(text);
    }

    protected Pattern getPattern() {
        return m_forwardingPattern;
    }

    public SyslogMessage parse() throws SyslogParserException {
        if (!this.find()) {
            if (traceEnabled()) {
                LogUtils.tracef(this, "'%s' did not match '%s'", m_forwardingPattern, getText());
            }
            return null;
        }

        final Matcher forwardingMatcher = getMatcher();

        final SyslogMessage message = new SyslogMessage();

        final Matcher syslogMatcher = m_syslogPattern.matcher(getText());
        if (syslogMatcher.matches()) {
            try {
                int priorityField = Integer.parseInt(syslogMatcher.group(1));
                message.setFacility(getFacility(priorityField));
                message.setSeverity(getSeverity(priorityField));
            } catch (final NumberFormatException e) {
                LogUtils.debugf(this, e, "Unable to parse priority field '%s' from text: %s", syslogMatcher.group(1), getText());
            }

            final String version = syslogMatcher.group(2);
            if (version != null && version.length() > 0) {
                try {
                    message.setVersion(Integer.parseInt(version));
                } catch (final NumberFormatException e) {
                    LogUtils.debugf(this, e, "Unable to parse version field '%s' from text: %s", version, getText());
                }
            }

            message.setMessageID(syslogMatcher.group(3));
            
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                message.setDate(df.parse(syslogMatcher.group(4)));
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "Unable to parse date '%s' from text: %s", syslogMatcher.group(4), getText());
            }

            message.setHostName(syslogMatcher.group(5));

            message.setProcessName(syslogMatcher.group(6));
            if (syslogMatcher.group(7) != null) {
                try {
                    final Integer pid = Integer.parseInt(syslogMatcher.group(7));
                    message.setProcessId(pid);
                } catch (final NumberFormatException nfe) {
                    LogUtils.debugf(this, nfe, "Unable to parse '%s' as a process ID.", syslogMatcher.group(7));
                }
            }
            message.setMessage(syslogMatcher.group(8));
        }

        if (message.getDate() == null) {
            message.setDate(new Date());
        }
        if (message.getHostName() == null) {
            message.setHostName(forwardingMatcher.group(m_matchingGroupHost));
        }
        if (message.getMessage() == null) {
            message.setMessage(forwardingMatcher.group(m_matchingGroupMessage));
        }

        return message;
    }
}

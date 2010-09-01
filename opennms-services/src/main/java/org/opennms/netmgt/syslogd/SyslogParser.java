package org.opennms.netmgt.syslogd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;


public class SyslogParser {
    private static Pattern m_pattern = Pattern.compile("^.*$");
    private Matcher m_matcher = null;
    private final String m_text;
    private Boolean m_found = null;
    private Boolean m_matched = null;
    private boolean m_traceEnabled = false;

    protected SyslogParser(final String text) {
        m_text = text;
        m_traceEnabled = LogUtils.isTraceEnabled(this);
    }

    public boolean find() {
        if (m_found == null) {
            getMatcher().reset();
            m_found = getMatcher().find();
        }
        return m_found;
    }

    public boolean matches() {
        if (m_matched == null) {
            getMatcher().reset();
            m_matched = getMatcher().matches();
        }
        return m_matched;
    }

    protected Boolean matched() {
        return m_matched;
    }

    protected String getText() {
        return m_text;
    }

    protected boolean traceEnabled() {
        return m_traceEnabled;
    }

    /* override this to return your own class */
    public static SyslogParser getParser(final String text) throws SyslogParserException {
        return new SyslogParser(text);
    }

    /* override this to get your custom pattern */
    protected Pattern getPattern() {
        return m_pattern;
    }

    /* override this to parse data from the matcher */
    public SyslogMessage parse() throws SyslogParserException {
        final SyslogMessage message = new SyslogMessage();
        message.setMessage(getMatcher().group());
        return message;
    }

    protected Matcher getMatcher() {
        if (m_matcher == null) {
            m_matcher = getPattern().matcher(m_text);
        }
        return m_matcher;
    }

    // useful methods for parser
    protected int getFacility(final int priorityField) {
        return ((priorityField & SyslogMessage.LOG_FACMASK) >> 3);
    }
    
    protected int getSeverity(final int priorityField) {
        return (priorityField & SyslogMessage.LOG_PRIMASK);
    }

}

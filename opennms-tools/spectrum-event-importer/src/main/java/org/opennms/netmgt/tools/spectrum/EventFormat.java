package org.opennms.netmgt.tools.spectrum;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventFormat {
    private String m_eventCode;
    private String m_contents;
    
    public EventFormat(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event-code parameter must not be null");
        }
        m_eventCode = eventCode;
    }

    public String getEventCode() {
        return m_eventCode;
    }

    public void setEventCode(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event-code must not be null");
        }
        m_eventCode = eventCode;
    }
    
    public String getContents() {
        return m_contents;
    }
    
    public void setContents(String contents) {
        if (contents == null) {
            throw new IllegalArgumentException("The contents must not be null");
        }
        m_contents = contents;
    }
    
    public List<String> getSubstTokens() {
        List<String> tokens = new ArrayList<String>();
        
        Matcher m = Pattern.compile("(?s)(\\{.*?\\})").matcher(m_contents);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        
        return tokens;
    }

}
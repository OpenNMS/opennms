package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author brozow
 *
 */
public class LineOrientedResponse {
    
    private String m_response;

    public void receive(BufferedReader in) throws IOException {
        m_response = in.readLine();
    }

    public boolean startsWith(String prefix) {
        return m_response != null && m_response.startsWith(prefix);
    }
    
    public boolean contains(String pattern) {
        return m_response != null && m_response.contains(pattern);
    }
    
    public boolean endsWith(String suffix) {
        return m_response != null && m_response.endsWith(suffix);
    }
    
    public boolean matches(String regex) {
        return m_response != null && m_response.matches(regex);
    }
    
    public boolean find(String regex) {
        return m_response != null && Pattern.compile(regex).matcher(m_response).find();
    }
    
    public boolean equals(String response) {
        return (response == null ? m_response == null : response.equals(m_response));
    }
    
    public String toString() {
        return String.format("Response: %s", m_response);
    }

}
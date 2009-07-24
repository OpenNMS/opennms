package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author brozow
 *
 */
public class LineOrientedResponse {
    
    private String m_response;
    
    public LineOrientedResponse(String response) {
        setResponse(response);
    }
    
    public void receive(BufferedReader in) throws IOException {
        setResponse(in.readLine());
    }

    public boolean startsWith(String prefix) {
        return getResponse() != null && getResponse().startsWith(prefix);
    }
    
    public boolean contains(String pattern) {
        return getResponse() != null && getResponse().contains(pattern);
    }
    
    public boolean endsWith(String suffix) {
        return getResponse() != null && getResponse().endsWith(suffix);
    }
    
    public boolean matches(String regex) {
        return getResponse() != null && getResponse().toString().trim().matches(regex);
    }
    
    public boolean find(String regex) {
        return getResponse() != null && Pattern.compile(regex).matcher(getResponse()).find();
    }
    
    public boolean equals(String response) {
        return (response == null ? getResponse() == null : response.equals(getResponse()));
    }
    
    public String toString() {
        return String.format("Response: %s", getResponse());
    }

    public void setResponse(String response) {
        m_response = response;
    }

    public String getResponse() {
        return m_response;
    }

}
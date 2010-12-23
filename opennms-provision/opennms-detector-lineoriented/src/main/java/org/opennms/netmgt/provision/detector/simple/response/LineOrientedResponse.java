package org.opennms.netmgt.provision.detector.simple.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * <p>LineOrientedResponse class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class LineOrientedResponse {
    
    private String m_response;
    
    /**
     * <p>Constructor for LineOrientedResponse.</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public LineOrientedResponse(final String response) {
        setResponse(response);
    }
    
    /**
     * <p>receive</p>
     *
     * @param in a {@link java.io.BufferedReader} object.
     * @throws java.io.IOException if any.
     */
    public void receive(final BufferedReader in) throws IOException {
        setResponse(in.readLine());
    }

    /**
     * <p>startsWith</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean startsWith(final String prefix) {
        return getResponse() != null && getResponse().startsWith(prefix);
    }
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean contains(final String pattern) {
        return getResponse() != null && getResponse().contains(pattern);
    }
    
    /**
     * <p>endsWith</p>
     *
     * @param suffix a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean endsWith(final String suffix) {
        return getResponse() != null && getResponse().endsWith(suffix);
    }
    
    /**
     * <p>matches</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean matches(final String regex) {
        return getResponse() != null && getResponse().toString().trim().matches(regex);
    }
    
    /**
     * <p>find</p>
     *
     * @param regex a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean find(final String regex) {
        return getResponse() != null && Pattern.compile(regex).matcher(getResponse()).find();
    }
    
    /**
     * <p>equals</p>
     *
     * @param response a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean equals(final String response) {
        return (response == null ? getResponse() == null : response.equals(getResponse()));
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return String.format("Response: %s", getResponse());
    }

    /**
     * <p>setResponse</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public void setResponse(final String response) {
        m_response = response;
    }

    /**
     * <p>getResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResponse() {
        return m_response;
    }

}

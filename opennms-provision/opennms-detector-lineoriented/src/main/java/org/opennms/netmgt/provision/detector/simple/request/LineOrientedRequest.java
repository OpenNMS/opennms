package org.opennms.netmgt.provision.detector.simple.request;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>LineOrientedRequest class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class LineOrientedRequest {
    
    /** Constant <code>Null</code> */
    public static final LineOrientedRequest Null = new LineOrientedRequest(null) {
        
    };
    
    private String m_command;
    
    /**
     * <p>Constructor for LineOrientedRequest.</p>
     *
     * @param command a {@link java.lang.String} object.
     */
    public LineOrientedRequest(final String command) {
        m_command = command;
    }

    /**
     * <p>send</p>
     *
     * @throws java.io.IOException if any.
     * @param out a {@link java.io.OutputStream} object.
     */
    public void send(final OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", m_command).getBytes());
    }
    
    /**
     * <p>getRequest</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRequest() {
        return String.format("%s\r\n", m_command);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return String.format("Request: %s", m_command);
    }

}

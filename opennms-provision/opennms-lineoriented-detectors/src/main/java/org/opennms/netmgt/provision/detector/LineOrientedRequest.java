package org.opennms.netmgt.provision.detector;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author brozow
 *
 */
public class LineOrientedRequest {
    
    public static final LineOrientedRequest Null = new LineOrientedRequest(null) {
        @Override
        public void send(OutputStream out) throws IOException {
        }
    };
    
    private String m_command;
    
    public LineOrientedRequest(String command) {
        m_command = command;
    }

    /**
     * @param socket
     * @throws IOException 
     */
    public void send(OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", m_command).getBytes());
    }
    
    public String getRequest() {
        return String.format("%s\r\n", m_command);
    }
    
    public String toString() {
        return String.format("Request: %s", m_command);
    }

}
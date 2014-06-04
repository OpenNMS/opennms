package org.opennms.debug;

import java.util.Date;

public class ConnectionInfo {
    private Exception m_exception;
    private Date m_datestamp;

    public ConnectionInfo() {
        m_exception = new Exception();
        m_datestamp = new Date();
    }
    
    public Exception getException() {
        return m_exception;
    }
    
    public Date getDate() {
        return m_datestamp;
    }
}

package org.opennms.netmgt.protocols.xmp;

public class XmpUtilException extends Exception {
    String m_message;
    
    public XmpUtilException(String msg) {
        m_message = msg;
    }
    
    public String getMessage() {
        return m_message;
    }
}

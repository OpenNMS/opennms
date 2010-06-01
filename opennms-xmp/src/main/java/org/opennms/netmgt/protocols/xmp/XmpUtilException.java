package org.opennms.netmgt.protocols.xmp;

public class XmpUtilException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7653583871376609217L;

    String m_message;
    
    public XmpUtilException(String msg) {
        m_message = msg;
    }
    
    public String getMessage() {
        return m_message;
    }
}

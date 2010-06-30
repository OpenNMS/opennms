package org.opennms.netmgt.protocols.xmp;

/**
 * <p>XmpUtilException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class XmpUtilException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -7653583871376609217L;
    
    String m_message;
    
    /**
     * <p>Constructor for XmpUtilException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public XmpUtilException(String msg) {
        m_message = msg;
    }
    
    /**
     * <p>getMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMessage() {
        return m_message;
    }
}

package org.opennms.netmgt.poller.nsclient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This object implements the packets created by the
 * <code>NsclientManager</code> system.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 */
public class NsclientPacket {
    /**
     * Stores the response from the server.
     */
    private String m_Response = "";

    /**
     * Stores the result of the check.
     */
    private short m_ResultCode = RES_STATE_UNKNOWN;

    /**
     * This value is used to state that the service check was validated OK.
     */
    public static final short RES_STATE_OK = 0;

    /**
     * This value is used to state that the service check was validated, but
     * needs attention.
     */
    public static final short RES_STATE_WARNING = 1;

    /**
     * This value is used to state the the service check was validated and
     * needs immediate attention, an outage has occured.
     */
    public static final short RES_STATE_CRIT = 2;

    /**
     * This value is used when a service check validation has unknown results.
     */
    public static final short RES_STATE_UNKNOWN = -1;

    /**
     * This member is used to convert result codes to strings and vice versa.
     */
    public static HashMap StateStrings = new HashMap();

    /**
     * Populates the member used for converting result codes to strings and
     * vice versa.
     */
    static {
        StateStrings.put(new String("OK"), new Short(RES_STATE_OK));
        StateStrings.put("WARNING", new Short(RES_STATE_WARNING));
        StateStrings.put("CRITICAL", new Short(RES_STATE_CRIT));
        StateStrings.put("UNKNOWN", new Short(RES_STATE_UNKNOWN));

    }

    /**
     * This method converts a result code to a string.
     * 
     * @param type
     *            the result code to convert
     * @return the string name of the result code passed, default "UNKNOWN" if
     *         no correspond code found.
     */
    public static String convertStateToString(short type) {
        Iterator iter = StateStrings.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            short val = ((Short) e.getValue()).shortValue();
            if (val == type)
                return (String) e.getKey();
        }
        return "UNKNOWN";
    }

    /**
     * This method returns the result code for a corresponding string.
     * 
     * @param type
     *            the string name of the result code.
     * @return the short ID for the result code.
     */
    public static short convertStringToType(String type) {
        return ((Short) StateStrings.get(type)).shortValue();
    }

    /**
     * Constructor, sets the response member.
     * 
     * @param response
     *            the response value from the server.
     */
    NsclientPacket(String response) {
        m_Response = response;
    }

    /**
     * Returns the value of the server response.
     * 
     * @return the value of the server response.
     */
    public String getResponse() {
        return m_Response;
    }

    /**
     * Returns the result code for the validation.
     * 
     * @return the result code for the validation.
     */
    public short getResultCode() {
        return m_ResultCode;
    }

    /**
     * This method sets the result code for the check validation.
     * @param res the result code.
     */
    public void setResultCode(short res) {
        m_ResultCode = res;
    }
}

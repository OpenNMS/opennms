/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.nsclient;

import java.util.HashMap;
import java.util.Map;

/**
 * This object implements the packets created by the
 * <code>NsclientManager</code> system.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @version $Id: $
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
     * needs immediate attention, an outage has occurred.
     */
    public static final short RES_STATE_CRIT = 2;

    /**
     * This value is used when a service check validation has unknown results.
     */
    public static final short RES_STATE_UNKNOWN = -1;

    /**
     * This member is used to convert result codes to strings and vice versa.
     */
    public static final Map<String,Short> STATE_STRINGS = new HashMap<String,Short>();

    /**
     * Populates the member used for converting result codes to strings and
     * vice versa.
     */
    static {
        STATE_STRINGS.put("OK", RES_STATE_OK);
        STATE_STRINGS.put("WARNING", RES_STATE_WARNING);
        STATE_STRINGS.put("CRITICAL", RES_STATE_CRIT);
        STATE_STRINGS.put("UNKNOWN", RES_STATE_UNKNOWN);

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
        for (Map.Entry<String,Short> e : STATE_STRINGS.entrySet()) {
            short val = e.getValue();
            if (val == type)
                return e.getKey();
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
        return ((Short) STATE_STRINGS.get(type)).shortValue();
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
     *
     * @param res the result code.
     */
    public void setResultCode(short res) {
        m_ResultCode = res;
    }
}

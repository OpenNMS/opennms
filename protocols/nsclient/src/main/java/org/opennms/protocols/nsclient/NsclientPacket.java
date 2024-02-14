/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

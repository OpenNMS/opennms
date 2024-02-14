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
package org.opennms.protocols.wmi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object implements the results received by the
 * <code>WmiManager</code> system.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @version $Id: $
 */
public class WmiResult {
	/**
	 * Stores the response from the server.
	 */
	private List<Object> m_Response = null;

	/**
	 * Stores the result of the check.
	 */
	private short m_ResultCode = RES_STATE_UNKNOWN;

	/**
	 * This value is used to state that the service check was validated OK.
	 */
	public static final short RES_STATE_OK = 0;

	/**
	 * This value is used to state the the service check was validated and
	 * needs immediate attention, an outage has occurred.
	 */
	public static final short RES_STATE_CRIT = 1;

	/**
	 * This value is used when a service check validation has unknown results.
	 */
	public static final short RES_STATE_UNKNOWN = -1;

	/**
	 * This member is used to convert result codes to strings and vice versa.
	 */
	public static Map<String, Short> StateStrings = new HashMap<String, Short>();

	/**
	 * Populates the member used for converting result codes to strings and
	 * vice versa.
	 */
	static {
		StateStrings.put("OK", RES_STATE_OK);
		StateStrings.put("CRITICAL", RES_STATE_CRIT);
		StateStrings.put("UNKNOWN", RES_STATE_UNKNOWN);

	}

	/**
	 * This method converts a result code to a string.
	 *
	 * @param type
	 *            the result code to convert
	 * @return the string name of the result code passed, default "UNKNOWN" if
	 *         no correspond code found.
	 */
	public static String convertStateToString(final short type) {
	    for (Map.Entry<String,Short> entry : StateStrings.entrySet()) {
	        final short val = entry.getValue();
	        if (val == type) {
	            return entry.getKey();
	        }
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
	public static short convertStringToType(final String type) {
		return StateStrings.get(type).shortValue();
	}

	/**
	 * Constructor, sets the response member.
	 * 
	 * @param response
	 *            the response value from the server.
	 */
	WmiResult(final List<Object> response) {
		m_Response = response;
	}

	/**
	 * Returns the value of the server response.
	 *
	 * @return the value of the server response.
	 */
	public List<Object> getResponse() {
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
	public void setResultCode(final short res) {
		m_ResultCode = res;
	}

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
        @Override
    public String toString() {
        return "WmiResult result code: " + convertStateToString(m_ResultCode);
    }
}

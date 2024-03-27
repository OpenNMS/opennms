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

/**
 * This class contains the parameters used to perform and validate checks
 * against the NSClient daemon.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @version $Id: $
 */
public class NsclientCheckParams {
    /**
     * Contains the warning threshold.
     */
    private int m_WarningPercent = 0;

    /**
     * Contains the critical threshold.
     */
    private int m_CriticalPercent = 0;

    /**
     * Contains a string holding parameters related to check items.
     */
    private String m_ParamString = "";

    /**
     * Constructor, sets the critical threshold.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     */
    public NsclientCheckParams(int critPerc) {
        m_CriticalPercent = critPerc;
    }

    /**
     * Constructor, sets the critical and warning thresholds.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     * @param warnPerc
     *            the value to be used for the warning threshold.
     */
    public NsclientCheckParams(int critPerc, int warnPerc) {
        m_CriticalPercent = critPerc;
        m_WarningPercent = warnPerc;
    }

    /**
     * Constructor, sets the critical and warning thresholds and the parameter
     * strings.
     *
     * @param critPerc
     *            the value to be used for the critical threshold.
     * @param warnPerc
     *            the value to be used for the warning threshold.
     * @param params
     *            the parameter string used for creating check requests.
     */
    public NsclientCheckParams(int critPerc, int warnPerc, String params) {
        m_CriticalPercent = critPerc;
        m_WarningPercent = warnPerc;
        m_ParamString = params;
    }

    /**
     * Constructor, sets the parameter string used when creating check
     * requests.
     *
     * @param params
     *            the parameter string used for creating check requests.
     */
    public NsclientCheckParams(String params) {
        m_ParamString = params;
    }

    /**
     * Returns the warning threshold value.
     *
     * @return the warning threshold value.
     */
    public int getWarningPercent() {
        return m_WarningPercent;
    }

    /**
     * Returns the critical threshold value.
     *
     * @return the critical threshold value.
     */
    public int getCriticalPercent() {
        return m_CriticalPercent;
    }

    /**
     * Returns the string containing the parameters for creating check
     * requests.
     *
     * @return the string containing the parameters for creating check
     *         requests.
     */
    public String getParamString() {
        return m_ParamString;
    }
}

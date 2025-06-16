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
package org.opennms.reporting.datablock;

/**
 * This class holds the service lost and regained time pair for the
 * node/ipaddr/service combination.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class Outage extends Object {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "reports";

    /**
     * Time at which the service was lost.
     */
    private long m_svcLostTime; // in milliseconds.

    /**
     * Time at which service was regained.
     */
    private long m_svcRegainedTime; // in milliseconds.

    /**
     * Default Constructor.
     */
    public Outage() {
        m_svcLostTime = -1;
        m_svcRegainedTime = -1;
    }

    /**
     * Constructor that sets the lost time.
     *
     * @param svcLost
     *            Time at which the service is lost.
     */
    public Outage(long svcLost) {
        m_svcLostTime = svcLost;
        m_svcRegainedTime = -1;
    }

    /**
     * Constructor that sets the service lost and regained times.
     *
     * @param svcLost
     *            Time at which the service is lost.
     * @param svcRegained
     *            Time at which the service is regained.
     */
    public Outage(long svcLost, long svcRegained) {
        m_svcLostTime = svcLost;
        m_svcRegainedTime = svcRegained;
    }

    /**
     * Set the Lost time
     *
     * @param losttime
     *            Time at which the service is lost.
     */
    public void setLostTime(long losttime) {
        m_svcLostTime = losttime;
    }

    /**
     * Set the regained time.
     *
     * @param regainedtime
     *            Time at which the service is regained.
     */
    public void setRegainedTime(long regainedtime) {
        m_svcRegainedTime = regainedtime;
    }

    /**
     * Return the service lost time
     *
     * @return the service lost time.
     */
    public long getLostTime() {
        return m_svcLostTime;
    }

    /**
     * Return the regained time
     *
     * @return the service regained time.
     */
    public long getRegainedTime() {
        return m_svcRegainedTime;
    }

    /**
     * Return the downtime (difference between the regained and lost times) in
     * the last rolling window
     *
     * @param curTime
     *            Time denoting end of rolling window (milliseconds).
     * @param rollingWindow
     *            Rolling Window (milliseconds)
     * @return the downtime (difference between the regained and lost times) in
     *         the last rolling window
     */
    public long getDownTime(long curTime, long rollingWindow) {
        long downTime = 0;

        // make sure the losttime is greater than current time.
        if (curTime < m_svcLostTime) {
            return downTime;
        }

        // the start of the rolling window
        long startTime = curTime - rollingWindow;

        if (m_svcRegainedTime == -1) {
            // node yet to regain service
            if (m_svcLostTime < startTime) {
                // if svclosttime is less than the rolling window
                // means its been down throughout
                downTime = rollingWindow;
            } else {
                downTime = curTime - m_svcLostTime;
            }
        } else {
            if (m_svcLostTime >= startTime) {
                if (m_svcRegainedTime < curTime) {
                    downTime = m_svcRegainedTime - m_svcLostTime;
                } else {
                    downTime = curTime - m_svcLostTime;
                }
            } else {
                if (m_svcRegainedTime < startTime) // Doesnt lie within rolling
                                                    // window.
                {
                    return 0;
                    // downTime = m_svcRegainedTime - startTime;
                } else {
                    if (m_svcRegainedTime > curTime) {
                        downTime = rollingWindow;
                    } else {
                        downTime = m_svcRegainedTime - startTime;
                    }
                }
            }
        }

        return downTime;
    }

    /**
     * Returns the outage information in date format.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder retVal = new StringBuilder();
        retVal.append(" Lost service ").append(new java.util.Date(m_svcLostTime));
        retVal.append(" Regained service ").append(new java.util.Date(m_svcRegainedTime) + "\n");
        return retVal.toString();
    }
}

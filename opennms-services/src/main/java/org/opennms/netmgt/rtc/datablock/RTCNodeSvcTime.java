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
package org.opennms.netmgt.rtc.datablock;

/**
 * This contains a service lost/regained set/pair for the node - i.e each
 * service lost time and the corresponding service regained time
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class RTCNodeSvcTime {
    /**
     * Time at which service was lost
     */
    private final long m_svcLostTime;

    /**
     * Time at which service was regained
     */
    private long m_svcRegainedTime;

    /**
     * Creates a time with the lost time
     *
     * @param lostTime
     *            the time at which service was lost
     */
    public RTCNodeSvcTime(long lostTime) {
        this(lostTime, -1);
    }

    /**
     * Creates the service time with both the lost and regained times
     *
     * @param lostTime
     *            the time at which service was lost
     * @param regainedTime
     *            the time at which service was regained
     */
    public RTCNodeSvcTime(long lostTime, long regainedTime) {
        m_svcLostTime = lostTime;
        setRegainedTime(regainedTime);
    }

    /**
     * Set the service regained time
     *
     * @param t
     *            the time at which service was regained
     */
    public void setRegainedTime(long t) {
        if (t <= 0) {
            m_svcRegainedTime = -1;
        } else if (t < m_svcLostTime) {
            throw new IllegalArgumentException("Cannot set outage end time to value less than outage start time: " + m_svcRegainedTime + " < " + m_svcLostTime);
        } else {
            m_svcRegainedTime = t;
        }
    }

    /**
     * Return the service lost time
     *
     * @return the service lost time
     */
    public long getLostTime() {
        return m_svcLostTime;
    }

    /**
     * Return the service regained time
     *
     * @return the service regained time
     */
    public long getRegainedTime() {
        return m_svcRegainedTime;
    }

    /**
     * Return true if this outage has expired.
     *
     * @return true if this outage has expired
     * 
     * @param startOfRollingWindow Epoch milliseconds that indicates the 
     *   beginning of the rolling outage window.
     */
    public boolean hasExpired(long startOfRollingWindow) {
        if (m_svcRegainedTime < 0) {
            // service currently down, return false
            return false;
        } else if (m_svcRegainedTime >= startOfRollingWindow) {
            // service was regained after the start of the rolling outage window, return false
            return false;
        } else {
            return true;
        }
    }

    /**
     * Return the downtime (difference between the regained and lost times) in
     * the last rolling window
     *
     * @return the downtime (difference between the regained and lost times) in
     *         the last rolling window
     * @param curTime a long.
     * @param rollingWindow a long.
     */
    public long getDownTime(long curTime, long rollingWindow) {

        // make sure the lost time is not later than current time!
        if (curTime < m_svcLostTime) {
            return 0;
        }

        // the start of the rolling window
        long startTime = curTime - rollingWindow;

        if (m_svcRegainedTime < 0 || m_svcRegainedTime >= curTime) {
            // node yet to regain service
            if (m_svcLostTime < startTime) {
                // if svclosttime is less than the rolling window
                // means its been down throughout
                return rollingWindow; // curTime - startTime
            } else {
                return curTime - m_svcLostTime;
            }
        } else {
            // node has regained service
            if (m_svcLostTime < startTime) {
                return m_svcRegainedTime - startTime;
            } else {
                return m_svcRegainedTime - m_svcLostTime;
            }
        }
    }
}

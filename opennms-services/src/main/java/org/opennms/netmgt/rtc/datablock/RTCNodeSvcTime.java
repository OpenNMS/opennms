/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

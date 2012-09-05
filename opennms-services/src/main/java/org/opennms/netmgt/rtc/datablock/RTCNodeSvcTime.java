/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class RTCNodeSvcTime extends Object {
    /**
     * Time at which service was lost
     */
    private long m_svcLostTime;

    /**
     * Time at which service was regained
     */
    private long m_svcRegainedTime;

    /**
     * Default constructor - initilializes the values
     */
    public RTCNodeSvcTime() {
        m_svcLostTime = -1;
        m_svcRegainedTime = -1;
    }

    /**
     * Creates a time with the lost time
     *
     * @param lostt
     *            the time at which service was lost
     */
    public RTCNodeSvcTime(long lostt) {
        m_svcLostTime = lostt;
        m_svcRegainedTime = -1;
    }

    /**
     * Creates the service time with both the lost and regained times
     *
     * @param lostt
     *            the time at which service was lost
     * @param regainedt
     *            the time at which service was regained
     */
    public RTCNodeSvcTime(long lostt, long regainedt) {
        m_svcLostTime = lostt;

        if (regainedt <= 0)
            m_svcRegainedTime = -1;
        else
            m_svcRegainedTime = regainedt;
    }

    /**
     * Set the service lost time
     *
     * @param t
     *            the time at which service was lost
     */
    public void setLostTime(long t) {
        m_svcLostTime = t;
    }

    /**
     * Set the service regained time
     *
     * @param t
     *            the time at which service was regained
     */
    public void setRegainedTime(long t) {
        if (t <= 0)
            m_svcRegainedTime = -1;
        else
            m_svcRegainedTime = t;
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
     * Return if this outages has expired
     *
     * @return if this outages has expired
     * @param startOfRollingWindow a long.
     */
    public boolean hasExpired(long startOfRollingWindow) {
        if (m_svcRegainedTime == -1) {
            // service currently down, return false
            return false;
        }

        if (m_svcLostTime < startOfRollingWindow && m_svcRegainedTime < startOfRollingWindow) {
            return true;
        }

        return false;
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
        long downTime = 0;

        // make sure the lost time is not later than current time!
        if (curTime < m_svcLostTime)
            return downTime;

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
            // node has regained service
            if (m_svcLostTime >= startTime) {
                downTime = m_svcRegainedTime - m_svcLostTime;
            } else if (m_svcRegainedTime > startTime) {
                downTime = m_svcRegainedTime - startTime;
            }
        }

        return downTime;
    }
}

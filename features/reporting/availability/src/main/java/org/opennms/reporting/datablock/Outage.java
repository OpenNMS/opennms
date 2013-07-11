/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

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
        StringBuffer retVal = new StringBuffer();
        retVal.append(" Lost service ").append(new java.util.Date(m_svcLostTime));
        retVal.append(" Regained service ").append(new java.util.Date(m_svcRegainedTime) + "\n");
        return retVal.toString();
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.util.LinkedList;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List of service times. This contains a list of service lost/regained set/pair
 * for the RTCNode.
 *
 * Also maintains the outage/down time each time it is calculated and the time
 * from which this was calculated - this is done so when the outage time for a
 * window is calculated, the same calculations are not done on the node multiple
 * times.
 *
 * 'Expired' outages are removed during 'add' and 'getDownTime' operations.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class RTCNodeSvcTimesList extends LinkedList<RTCNodeSvcTime> {
    private static final Logger LOG = LoggerFactory.getLogger(RTCNodeSvcTimesList.class);
    private static final long serialVersionUID = 2606739258065019820L;

    /**
     * The time from which the current outtime 'm_outTime' is calculated
     */
    private final long m_rollingWindow;

    /**
     * Remove expired outages. Remove all closed outages that are not in the the
     * last 'rollingWindow'
     */
    private void removeExpiredOutages() {
        long curTime = System.currentTimeMillis();

        removeExpiredOutages(curTime, m_rollingWindow);
    }

    /**
     * Remove expired outages. Remove closed outages that are not in the the
     * last 'rollingWindow' starting from curTime.
     * 
     * @param curTime
     *            the current time to start from.
     * @param rollingWindow
     *            the rolling window to use.
     */
    private void removeExpiredOutages(long curTime, long rollingWindow) {
        // the start of the rolling window
        long startTime = curTime - rollingWindow;

        for (ListIterator<RTCNodeSvcTime> iter = listIterator(); iter.hasNext();) {
            RTCNodeSvcTime svcTime = (RTCNodeSvcTime) iter.next();

            // since new outages are added at the end, if this outage
            // has not expired we can safely break from the loop
            if (svcTime.getLostTime() >= startTime) {
                break;
            }

            if (svcTime.hasExpired(startTime)) {
                iter.remove();
            }
        }
    }

    /**
     * Default constructor.
     */
    public RTCNodeSvcTimesList(long rollingWindow) {
        super();

        m_rollingWindow = rollingWindow;
    }

    /**
     * Add a new servicetime entry.
     *
     * @param losttime
     *            time at which service was lost
     * @param regainedtime
     *            time at which service was regained
     */
    public void addSvcTime(long losttime, long regainedtime) {
        // remove expired outages
        removeExpiredOutages();

        if (regainedtime > 0 && regainedtime < losttime) {
            LOG.warn("RTCNodeSvcTimesList: Rejecting service time pair since regained time {}\tregainedtime in milliseconds: {} less than lost time -> losttime in milliseconds: {}", regainedtime, losttime);

            return;
        }

        addLast(new RTCNodeSvcTime(losttime, regainedtime));
    }

    /**
     * Add a new servicetime entry
     *
     * @param losttime
     *            time at which service was lost
     */
    public void addSvcTime(long losttime) {
        // remove expired outages
        removeExpiredOutages();

        addLast(new RTCNodeSvcTime(losttime));
    }

    /**
     * Calculate the total downtime in this list of service times for the last
     * 'rollingWindow' time starting at 'curTime'
     *
     * @param curTime
     *            the current time from which the down time is to be calculated
     * @param rollingWindow
     *            the last window for which the downtime is to be calculated
     * @return total down time for all outages for this service
     */
    public long getDownTime(long curTime, long rollingWindow) {

        // remove expired outages
        removeExpiredOutages(curTime, rollingWindow);

        long outTime = 0;

        for (RTCNodeSvcTime svcTime : this) {
            outTime += svcTime.getDownTime(curTime, rollingWindow);
        }

        return outTime;
    }
}

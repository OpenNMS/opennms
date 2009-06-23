//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.rtc.datablock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.RTCConfigFactory;

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
    private static final long serialVersionUID = 1L;

    /**
     * The time from which the current outtime 'm_outTime' is calculated
     */
    private long m_outTimeSince;

    /**
     * The outage time computed since 'm_outTimeSince'
     */
    private long m_outTime;

    /**
     * Remove expired outages. Remove all closed outages that are not in the the
     * last 'rollingWindow'
     */
    private void removeExpiredOutages() {
        long curTime = System.currentTimeMillis();
        long rollingWindow = RTCConfigFactory.getInstance().getRollingWindow();

        removeExpiredOutages(curTime, rollingWindow);
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

        ListIterator<RTCNodeSvcTime> iter = listIterator();
        while (iter.hasNext()) {
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
    public RTCNodeSvcTimesList() {
        super();

        m_outTimeSince = -1;

        m_outTime = 0;
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
            Category log = ThreadCategory.getInstance(getClass());
            log.warn("RTCNodeSvcTimesList: Rejecting service time pair since regained time " + "less than lost time -> losttime in milliseconds: " + losttime + "\tregainedtime in milliseconds: " + regainedtime);

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
     * 
     * @return total down time in service times in this list
     */
    public long getDownTime(long curTime, long rollingWindow) {
        // calculate effective start time
        long startTime = curTime - rollingWindow;
        if (m_outTimeSince == startTime) {
            return m_outTime;
        }

        m_outTimeSince = startTime;

        m_outTime = 0;

        // remove expired outages
        removeExpiredOutages(curTime, rollingWindow);

        Iterator<RTCNodeSvcTime> iter = iterator();
        while (iter.hasNext()) {
            RTCNodeSvcTime svcTime = (RTCNodeSvcTime) iter.next();

            m_outTime += svcTime.getDownTime(curTime, rollingWindow);
        }

        return m_outTime;
    }
}

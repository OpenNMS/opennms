//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 14: Use Java 5 generics and indent. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>OwnedIntervalSequence class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OwnedIntervalSequence extends TimeIntervalSequence {

    /**
     * <p>Constructor for OwnedIntervalSequence.</p>
     */
    public OwnedIntervalSequence() {
        super();
    }

    /**
     * <p>Constructor for OwnedIntervalSequence.</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.OwnedInterval} object.
     */
    public OwnedIntervalSequence(OwnedInterval interval) {
        super(interval);
    }

    /** {@inheritDoc} */
    protected Collection<OwnedInterval> combineIntervals(TimeInterval currInt, TimeInterval newInt) {
        OwnedInterval currInterval = (OwnedInterval) currInt;
        OwnedInterval newInterval = (OwnedInterval) newInt;
        
        List<OwnedInterval> newIntervals = new ArrayList<OwnedInterval>(3);
        
        // Time Intervals stored locally so we can add them in order
        OwnedInterval firstSeg = null;
        OwnedInterval midSeg = null;
        OwnedInterval thirdSeg = null;
        
        // start and end of the middle segment is computed as we examine the first and last segments
        Date midSegStart;
        Date midSegEnd;
        
        // first we deal with the segment 1
        if (currInterval.getStart().equals(newInterval.getStart())) {
            // we have no first seg so the mid seg starts at the common top
            midSegStart = currInterval.getStart();
        } else {
            // this implies there is a top segment
            if (currInterval.getStart().before(newInterval.getStart())) {
                // first seg is the top of the currinterval
                firstSeg = new OwnedInterval(currInterval.getOwners(), currInterval.getStart(), newInterval.getStart());
                midSegStart = newInterval.getStart();
            } else {
                // first seg is the top of new interval
                firstSeg = new OwnedInterval(newInterval.getOwners(), newInterval.getStart(), currInterval.getStart());
                midSegStart = currInterval.getStart();
            }
        }
        
        // next we deal with segment 3
        if (currInterval.getEnd().equals(newInterval.getEnd())) {
            midSegEnd = currInterval.getEnd();
        } else {
            // this implies we have a third seg
            if (currInterval.getEnd().after(newInterval.getEnd())) {
                // third seg is the bottom of the curr interval
                thirdSeg = new OwnedInterval(currInterval.getOwners(), newInterval.getEnd(), currInterval.getEnd());
                midSegEnd = newInterval.getEnd();
            } else {
                // third seg is the bottom of the new interval
                thirdSeg = new OwnedInterval(newInterval.getOwners(), currInterval.getEnd(), newInterval.getEnd());
                midSegEnd = currInterval.getEnd();
            }
        }
        
        // now we create the middle seg with combined ownership
        midSeg = new OwnedInterval(currInterval.getOwners(), midSegStart, midSegEnd);
        midSeg.addOwners(newInterval.getOwners());
        
        if (firstSeg != null) {
            newIntervals.add(firstSeg);
        }
        if (midSeg != null) {
            newIntervals.add(midSeg);
        }
        if (thirdSeg != null) {
            newIntervals.add(thirdSeg);
        }
        
        
        return newIntervals;
    }

    /** {@inheritDoc} */
    protected TimeInterval createInterval(Date start, Date end) {
        return new OwnedInterval(start, end);
    }

    /** {@inheritDoc} */
    protected TimeIntervalSequence createTail(TimeInterval interval) {
        return new OwnedIntervalSequence((OwnedInterval) interval);
    }

    /** {@inheritDoc} */
    protected Collection<OwnedInterval> separateIntervals(TimeInterval origInt, TimeInterval removedInt) {
        OwnedInterval origInterval = (OwnedInterval) origInt;
        OwnedInterval removedInterval = (OwnedInterval) removedInt;
        
        // if the original is owned and no owners will be removed keep in intact
        List<Owner> reducedOwners = new ArrayList<Owner>(origInterval.getOwners());
        reducedOwners.removeAll(removedInterval.getOwners());
        if (origInterval.isOwned() && removedInterval.isOwned() && reducedOwners.equals(origInterval.getOwners())) {
            // the removedInterval did not have any owners in common with the original interval 
            // so leave the interval intact
            return Collections.singletonList(origInterval);
        }
        
        // if we got here then there is some ownership change in the original interval
        
        // there are potentially three new intervals
        OwnedInterval firstSeg = null;
        OwnedInterval midSeg = null;
        OwnedInterval lastSeg = null;
        
        Date midSegStart = null;
        Date midSegEnd = null;
        
        // first the first Segment
        if (origInterval.getStart().before(removedInterval.getStart())) {
            // then we have a firstSeg that has the original ownership
            // this causes the midSeg to start at the start of the removed interval
            midSegStart = removedInterval.getStart();
            firstSeg = new OwnedInterval(origInterval.getOwners(), origInterval.getStart(), midSegStart);
        } else {
            // there is no first seg so set mid seg to start at top of original interval
            midSegStart = origInterval.getStart();
        }

        
        // now the last segment
        if (removedInterval.getEnd().before(origInterval.getEnd())) {
            midSegEnd = removedInterval.getEnd();
            lastSeg = new OwnedInterval(origInterval.getOwners(), midSegEnd, origInterval.getEnd());
        } else {
            midSegEnd = origInterval.getEnd();
        }
        
        // we only add the midSegment if there are remaining owners
        if (removedInterval.isOwned() && !reducedOwners.isEmpty()) {
            midSeg = new OwnedInterval(reducedOwners, midSegStart, midSegEnd);
        }
        
        List<OwnedInterval> newIntervals = new ArrayList<OwnedInterval>(3);
        if (firstSeg != null) {
            newIntervals.add(firstSeg);
        }
        if (midSeg != null) {
            newIntervals.add(midSeg);
        }
        if (lastSeg != null) {
            newIntervals.add(lastSeg);
        }
        
        return newIntervals;
    }

}

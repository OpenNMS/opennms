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
package org.opennms.core.utils;

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
public class OwnedIntervalSequence extends AbstractTimeIntervalSequence<OwnedInterval> {

    /**
     * <p>Constructor for OwnedIntervalSequence.</p>
     */
    public OwnedIntervalSequence() {
        super();
    }

    /**
     * <p>Constructor for OwnedIntervalSequence.</p>
     *
     * @param interval a {@link org.opennms.core.utils.OwnedInterval} object.
     */
    public OwnedIntervalSequence(OwnedInterval interval) {
        super(interval);
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<OwnedInterval> combineIntervals(OwnedInterval currInterval, OwnedInterval newInterval) {
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
    @Override
    protected OwnedInterval createInterval(Date start, Date end) {
        return new OwnedInterval(start, end);
    }

    /** {@inheritDoc} */
    @Override
    protected OwnedIntervalSequence createTail(OwnedInterval interval) {
        return new OwnedIntervalSequence((OwnedInterval) interval);
    }

    /** {@inheritDoc} */
    @Override
    protected Collection<OwnedInterval> separateIntervals(OwnedInterval origInterval, OwnedInterval removedInterval) {
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

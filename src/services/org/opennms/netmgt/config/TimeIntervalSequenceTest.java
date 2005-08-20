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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sun.tools.tree.NewInstanceExpression;



public class TimeIntervalSequenceTest extends IntervalTestCase {
    



    public void testEmptySequence() {
        TimeIntervalSequence seq = new TimeIntervalSequence();
        // expect a sequence of no intervals
        assertTimeIntervalSequence(new TimeInterval[0], seq);
    }
    
    public void testSingletonSeq() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18));
        // expect a sequence of one interval
        assertTimeIntervalSequence(new TimeInterval[] { aug(18) }, seq);
    }

    
    public void testAddToEmptySeq() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence();
        seq.addInterval(aug(18));
        
        // expect a sequence containing the added interval
        assertTimeIntervalSequence(new TimeInterval[] { aug(18) }, seq);
    }
    
    public void testAddNonOverlappingLater() throws Exception {
        
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18));
        seq.addInterval(aug(19, 11, 12));
        // expect a sequence containing the original followed by the added
        assertTimeIntervalSequence(new TimeInterval[] { aug(18), aug(19, 11, 12) }, seq);
        
    }
    
    public void testAddNonOverlappingEarlier() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18));
        seq.addInterval(aug(17, 11, 12));
        // expect a sequence containing the added followed by the original
        assertTimeIntervalSequence(new TimeInterval[] {aug(17, 11, 12), aug(18) }, seq);
    }
    
    public void testAddEquals() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 8, 17));
        // expect a sequence containing non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 17) }, seq);
    }
    
    public void testAddOverlappingCentered() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 11, 12));
        // expect a sequence containing non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] {aug(18, 8, 11), aug(18, 11, 12), aug(18, 12, 17) }, seq);
        
    }
    
    public void testAddOverlappingLater() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 11, 20));
        // expect a sequence containing non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] {aug(18, 8, 11), aug(18, 11, 17), aug(18, 17, 20) }, seq);
        
    }
    
    public void testAddOverlappingEarlier() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 3, 12));
        // expect a sequence containing non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] {aug(18, 3, 8), aug(18, 8, 12), aug(18, 12, 17) }, seq);
        
    }

    public void testAddOverlappingStraddle() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 3, 20));
        // expect a sequence containing non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] {aug(18, 3, 8), aug(18, 8, 17), aug(18, 17, 20) }, seq);
        
    }
    
    public void testAddOverlappingFirstSegmentEmpty() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 8, 20));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 17), aug(18, 17, 20) }, seq);
    }
    
    public void testAddOverlappingLastSegmentEmpty() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(18, 3, 17));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 3, 8), aug(18, 8, 17) }, seq);
    }
    
    public void testAddOverlappingMany() throws Exception {
        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 8, 17));
        seq.addInterval(aug(20, 8, 17));
        
        // now add interval from aug 18 0 to aug 20 24)
        seq.addInterval(aug(18, 0, 20, 24));
        
        // expect a sequence containing non overlapping segments
        TimeInterval[] expected = {
                aug(18, 0, 8),
                aug(18, 8, 17),
                aug(18, 17, 19, 8),
                aug(19, 8, 17),
                aug(19, 17, 20, 8),
                aug(20, 8, 17),
                aug(20, 17, 24)
        };
        assertTimeIntervalSequence(expected, seq);
        
    }
    
    public void testRemoveOnlyInterval() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 8, 17));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[0], seq);
    }
    
    public void testRemoveFirstEntireInterval() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 11, 12));

        seq.removeInterval(aug(18, 8, 17));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(19, 11, 12) }, seq);
    }
    
    public void testRemoveMiddleEntireInterval() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 11, 12));
        seq.addInterval(aug(20, 11, 12));

        seq.removeInterval(aug(19, 11, 12));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 17), aug(20, 11, 12) }, seq);
    }


    public void testRemoveLastEntireInterval() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 11, 12));

        seq.removeInterval(aug(19, 11, 12));
        // expect a sequence containing only two non overlapping segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 17) }, seq);
    }
    
    public void testRemoveCenteredOverlappingInterval() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 11, 12));

        // expect a sequence containing only two surrounding segments
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 11), aug(18, 12, 17) }, seq);
    }
    
    public void testRemoveOverlappingLater() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 11, 20));

        // expect a sequence containing only one preceeding segment
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 11) }, seq);
        
    }

    public void testRemoveOverlappingEarlier() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 3, 12));

        // expect a sequence containing only one trailing segment
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 12, 17) }, seq);
        
    }
    
    public void testRemoveOverlappingStraddle() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 3, 20));

        // expect a sequence containing no remaining intervals
        assertTimeIntervalSequence(new TimeInterval[] { }, seq);
        
    }

    public void testRemoveOverlappingDegeneratePrefix() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 8, 12));

        // expect a sequence containing only the suffix
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 12, 17) }, seq);
        
    }

    public void testRemoveOverlappingDegenerateSuffix() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.removeInterval(aug(18, 12, 17));

        // expect a sequence containing only the prefix
        assertTimeIntervalSequence(new TimeInterval[] { aug(18, 8, 12) }, seq);
        
    }
    
    public void testRemoveOverlappingMany() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 8, 17));
        seq.addInterval(aug(20, 8, 17));
        seq.addInterval(aug(21, 8, 17));
        seq.addInterval(aug(22, 8, 17));
        
        // remove a large interval
        seq.removeInterval(aug(19, 12, 21, 12));
        
        // expect a sequence containing non overlapping segments
        TimeInterval[] expected = {
                aug(18, 8, 17),
                aug(19, 8, 12),
                aug(21, 12, 17),
                aug(22, 8, 17)
        };
        assertTimeIntervalSequence(expected, seq);
        
    }
    
    public void testBound() throws Exception {
        
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 8, 17));
        seq.addInterval(aug(20, 8, 17));
        seq.addInterval(aug(21, 8, 17));
        seq.addInterval(aug(22, 8, 17));
        
        // bound the sequence to the interval aug19 noon to aug 21 noon
        seq.bound(aug(19, 12, 21, 12));
        
        // expect a sequence containing non overlapping segments
        TimeInterval[] expected = {
                aug(19, 12, 17),
                aug(20, 8, 17),
                aug(21, 8, 12)
        };
        assertTimeIntervalSequence(expected, seq);
        
    }
    
    
    public void testGetBounds() throws Exception {
        TimeIntervalSequence seq = new TimeIntervalSequence(aug(18, 8, 17));
        seq.addInterval(aug(19, 8, 17));
        seq.addInterval(aug(20, 8, 17));
        seq.addInterval(aug(21, 8, 17));
        seq.addInterval(aug(22, 8, 17));

        assertEquals(aug(18, 8, 22, 17), seq.getBounds());
    }
    
    private class OwnedInterval extends TimeInterval {
        private List m_owners;
        
        public OwnedInterval(OwnedInterval owned) {
            this(owned.getOwners(), owned.getStart(), owned.getEnd());
        }

        public OwnedInterval(TimeInterval interval) {
            this(interval.getStart(), interval.getEnd());
        }
        
        public OwnedInterval(String owner, TimeInterval interval) {
            this(owner, interval.getStart(), interval.getEnd());
        }
        
        public OwnedInterval(List owners, TimeInterval interval) {
            this(owners, interval.getStart(), interval.getEnd());
        }
        
        public OwnedInterval(Date start, Date end) {
            this(Collections.EMPTY_LIST, start, end);
        }
        
        public OwnedInterval(String owner, Date start, Date end) {
            this(Collections.singletonList(owner), start, end);
        }
        
        public OwnedInterval(List owners, Date start, Date end) {
            super(start, end);
            m_owners = new ArrayList(owners);
            Collections.sort(m_owners);
        }
        
        public List getOwners() { return m_owners; }
        
        public void addOwner(String owner) { m_owners.add(owner); Collections.sort(m_owners); }
        
        public void removeOwner(String owner) { m_owners.remove(owner); }
        
        public void addOwners(List owners) { m_owners.addAll(owners); Collections.sort(m_owners); }
        
        public void removeOwners(List owners) { m_owners.removeAll(owners); }
        
        public boolean isOwner(String owner) { return m_owners.contains(owner); }
        
        public boolean isOwned() { return !m_owners.isEmpty(); }
        
        public String toString() {
            String ownerString = "";
            if (m_owners.isEmpty()) {
                ownerString = "UNOWNED";
            } else {
                for(int i = 0; i < m_owners.size(); i++) {
                    if (i != 0) ownerString += ",";
                    ownerString += m_owners.get(i);
                }
            }
            return ownerString+super.toString();
        }
        
        public int hashCode() { return 123; }
        
        public boolean equals(Object o) {
            if (o instanceof OwnedInterval) {
                OwnedInterval owned = (OwnedInterval) o;
                return super.equals(owned) && m_owners.equals(owned.m_owners);
            }
            return false;
        }
        
    }

    public class OwnedIntervalSequence extends TimeIntervalSequence {

        public OwnedIntervalSequence() {
            super();
        }

        public OwnedIntervalSequence(OwnedInterval interval) {
            super(interval);
        }
        
        protected Collection combineIntervals(TimeInterval currInt, TimeInterval newInt) {
            OwnedInterval currInterval = (OwnedInterval)currInt;
            OwnedInterval newInterval = (OwnedInterval) newInt;
            
            List newIntervals = new ArrayList(3);
            
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
            
            if (firstSeg != null) newIntervals.add(firstSeg);
            if (midSeg != null) newIntervals.add(midSeg);
            if (thirdSeg != null) newIntervals.add(thirdSeg);
            
            
            return newIntervals;
        }

        protected TimeInterval createInterval(Date start, Date end) {
            return new OwnedInterval(start, end);
        }

        protected TimeIntervalSequence createTail(TimeInterval interval) {
            return new OwnedIntervalSequence((OwnedInterval)interval);
        }

        protected Collection separateIntervals(TimeInterval origInt, TimeInterval removedInt) {
            OwnedInterval origInterval = (OwnedInterval) origInt;
            OwnedInterval removedInterval = (OwnedInterval) removedInt;
            
            // if the original is owned and no owners will be removed keep in intact
            List reducedOwners = new ArrayList(origInterval.getOwners());
            reducedOwners.removeAll(removedInterval.getOwners());
            if (origInterval.isOwned() && reducedOwners.equals(origInterval.getOwners())) {
                // the removedInterval did not have any owners in common with the orignal interval 
                // so leave the interval intact
                return Collections.singletonList(origInterval);
            }
            
            // if we got here then there is some onwership change in the original interval
            
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
            if (!reducedOwners.isEmpty())
                midSeg = new OwnedInterval(reducedOwners, midSegStart, midSegEnd);
            
            List newIntervals = new ArrayList(3);
            if (firstSeg != null) newIntervals.add(firstSeg);
            if (midSeg != null) newIntervals.add(midSeg);
            if (lastSeg != null) newIntervals.add(lastSeg);
            
            return newIntervals;
        }

    }

    
    private OwnedInterval owned(String owner, TimeInterval interval) {
        return (owner == null ? new OwnedInterval(interval) : new OwnedInterval(owner, interval));
    }
    
    private OwnedInterval owned(TimeInterval interval) {
        return owned(null, interval);
    }
    
    private OwnedInterval ownedOne(TimeInterval interval) {
        return owned("one", interval);
    }
    
    private OwnedInterval ownedTwo(TimeInterval interval) {
        return owned("two", interval);
    }
    
    private OwnedInterval ownedOneAndTwo(TimeInterval interval) {
        String[] owners = new String[] { "one", "two" };
        return new OwnedInterval(Arrays.asList(owners), interval);
    }
    
    public void testExtensionAdd() throws Exception {
        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        OwnedIntervalSequence seq = new OwnedIntervalSequence(ownedOne(aug(18, 8, 17)));
        seq.addInterval(ownedOne(aug(19, 8, 17)));
        seq.addInterval(ownedOne(aug(20, 8, 17)));
        
        // now add interval from aug 18 12 to aug 20 24)
        seq.addInterval(ownedTwo(aug(18, 12, 20, 24)));
        
        // expect a sequence containing non overlapping segments
        OwnedInterval[] expected = {
                ownedOne(aug(18, 8, 12)),
                ownedOneAndTwo(aug(18, 12, 17)),
                ownedTwo(aug(18, 17, 19, 8)),
                ownedOneAndTwo(aug(19, 8, 17)),
                ownedTwo(aug(19, 17, 20, 8)),
                ownedOneAndTwo(aug(20, 8, 17)),
                ownedTwo(aug(20, 17, 24))
        };
        assertTimeIntervalSequence(expected, seq);
        
    }
    
    public void testExtensionRemoveWrongOwner() throws Exception {

        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        OwnedIntervalSequence seq = new OwnedIntervalSequence(ownedOne(aug(18, 8, 17)));
        seq.addInterval(ownedOne(aug(19, 8, 17)));
        seq.addInterval(ownedOne(aug(20, 8, 17)));
        
        // now add interval from aug 18 12 to aug 20 24)
        seq.removeInterval(ownedTwo(aug(18, 12, 20, 24)));
        
        // expect a sequence containing non overlapping segments
        OwnedInterval[] expected = {
                ownedOne(aug(18, 8, 17)),
                ownedOne(aug(19, 8, 17)),
                ownedOne(aug(20, 8, 17)),
        };
        assertTimeIntervalSequence(expected, seq);
        

    }

    public void testExtensionRemoveOneOwner() throws Exception {

        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        OwnedIntervalSequence seq = new OwnedIntervalSequence(ownedOneAndTwo(aug(18, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(19, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(20, 8, 17)));
        
        // now add interval from aug 18 12 to aug 20 24)
        seq.removeInterval(ownedTwo(aug(18, 12, 20, 13)));
        
        // expect a sequence containing non overlapping segments
        OwnedInterval[] expected = {
                ownedOneAndTwo(aug(18, 8, 12)),
                ownedOne(aug(18, 12, 17)),
                ownedOne(aug(19, 8, 17)),
                ownedOne(aug(20, 8, 13)),
                ownedOneAndTwo(aug(20, 13, 17)),
        };
        assertTimeIntervalSequence(expected, seq);
        

    }

    public void testExtensionRemoveAllOwners() throws Exception {

        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        OwnedIntervalSequence seq = new OwnedIntervalSequence(ownedOneAndTwo(aug(18, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(19, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(20, 8, 17)));
        
        // now add interval from aug 18 12 to aug 20 24)
        seq.removeInterval(ownedOneAndTwo(aug(18, 12, 20, 13)));
        
        // expect a sequence containing non overlapping segments
        OwnedInterval[] expected = {
                ownedOneAndTwo(aug(18, 8, 12)),
                ownedOneAndTwo(aug(20, 13, 17)),
        };
        assertTimeIntervalSequence(expected, seq);
        

    }
    
    public void testExtensionUnscheduled() throws Exception {
        
        // seg with three entryies 8-5 on aug 18, aug 19 and aug 20
        OwnedIntervalSequence seq = new OwnedIntervalSequence(ownedOneAndTwo(aug(18, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(19, 8, 17)));
        seq.addInterval(ownedOneAndTwo(aug(20, 8, 17)));

        OwnedIntervalSequence unscheduled = new OwnedIntervalSequence(owned(aug(18)));
        unscheduled.removeAll(seq);
        
        OwnedInterval[] expected = {
                owned(aug(18, 0, 8)),
                owned(aug(18, 17, 24))
        };
        assertTimeIntervalSequence(expected, unscheduled);

    }
    

}

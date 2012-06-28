/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.test;

import java.util.Date;

import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.TimeInterval;
import org.opennms.core.utils.TimeIntervalSequence;

public class TimeIntervalSequenceTest extends IntervalTestCase {
    

    public void testPreceeds() throws Exception {
        TimeInterval first = new TimeInterval(new Date(0), date("18-08-2005 00:00:00"));
        TimeInterval second = aug(18);
        assertTrue(first.preceeds(second));
        assertFalse(second.preceeds(first));
    }
    
    public void testFollows() throws Exception {
        TimeInterval first = new TimeInterval(new Date(0), date("18-08-2005 00:00:00"));
        TimeInterval second = aug(18);
        assertTrue(second.follows(first));
        assertFalse(first.follows(second));
    }

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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author brozow
 *
 */
public class EventOrdering implements Comparable<EventOrdering>{
    
    public static class EventOrderIndex implements Comparable<EventOrderIndex> {
        
        private final EventOrdering m_ordering;
        private final int m_index;

        private EventOrderIndex(EventOrdering ordering, int index) {
            m_ordering = ordering;
            m_index = index;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(EventOrderIndex orderIndex) {
            int parentOrder = m_ordering.compareTo(orderIndex.m_ordering);
            if (parentOrder != 0) return parentOrder;
            
            return m_index - orderIndex.m_index;
        }
        
    }
    
    private final EventOrdering m_parent;
    private final int m_sequenceIndex;
    
    private final AtomicInteger m_nextSubsequence = new AtomicInteger(0);
    private final AtomicInteger m_nextIndex = new AtomicInteger(0);
    
    public EventOrdering() {
        this(null, 0);
    }
    

    private EventOrdering(EventOrdering parent, int sequenceIndex) {
        m_parent = parent;
        m_sequenceIndex = sequenceIndex;
    }

    /**
     * @return
     */
    public EventOrderIndex next() {
        int nextIndex = m_nextIndex.getAndIncrement();
        return new EventOrderIndex(this, nextIndex);
    }

    /**
     * @return
     */
    public EventOrdering subsequence() {
        int nextSubsequence = m_nextSubsequence.getAndIncrement();
        return new EventOrdering(this, nextSubsequence);
        
    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(EventOrdering o) {
        int parentCompare = compareParents(m_parent, o.m_parent);
        if (parentCompare != 0) return parentCompare;
        
        return m_sequenceIndex - o.m_sequenceIndex;
    }
    
    private int compareParents(EventOrdering parent1, EventOrdering parent2) {
        if (parent1 == parent2) return 0;
        if (parent1 == null) return -1;
        if (parent2 == null) return 1;
        return parent1.compareTo(parent2);
    }
    
    

}

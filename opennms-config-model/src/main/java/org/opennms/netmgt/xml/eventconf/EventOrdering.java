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
package org.opennms.netmgt.xml.eventconf;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.MoreObjects;

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
        
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("parent", m_ordering).add("idx", m_index).toString();
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
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("parent", m_parent)
                .add("idx", m_sequenceIndex)
                .toString();
    }

}

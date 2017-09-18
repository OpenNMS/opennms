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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.opennms.netmgt.xml.eventconf.EventOrdering.EventOrderIndex;

/**
 * @author brozow
 *
 */
public class EventOrderingTest {
    
    private class Item implements Comparable<Item>{
        private int m_label;
        private EventOrderIndex m_index;
        
        public Item(int label, EventOrderIndex index) {
            m_label = label;
            m_index = index;
        }
        
        public String toString() {
            return String.valueOf(m_label);
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(Item o) {
            return m_index.compareTo(o.m_index);
        }

        /**
         * @return
         */
        public Integer getLabel() {
            return m_label;
        }
    }

    @Test
    public void test() {
        
        SortedSet<Item> sorted = new TreeSet<>();
        
        EventOrdering ordering = new EventOrdering();
        
        List<Item> items = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            EventOrdering s1 = ordering.subsequence();
            for(int j = 0; j < 3; j++) {
                EventOrdering s2 = s1.subsequence();
                for(int k = 0; k < 5; k++) {
                    items.add(new Item(i*15+j*5+k, s2.next()));
                }
            }
        }
        
        Collections.shuffle(items);
        
        System.err.println(items);
        
        sorted.addAll(items);
        
        System.err.println(sorted);

        assertEquals(range(0,44), labels(sorted));
        
        
    }
    
    List<Integer> range(int begin, int end) {
        List<Integer> r = new ArrayList<>();
        for(int i = begin; i <= end; i++) {
            r.add(i);
        }
        return r;
    }
    
    List<Integer> labels(Collection<Item> items) {
        List<Integer> results = new ArrayList<>();
        for(Item item : items) {
            results.add(item.getLabel());
        }
        return results;
    }

}

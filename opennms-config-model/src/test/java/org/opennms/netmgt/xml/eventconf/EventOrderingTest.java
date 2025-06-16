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

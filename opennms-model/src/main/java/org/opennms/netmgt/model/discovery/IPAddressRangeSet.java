/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.discovery;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class IPAddressRangeSet implements Iterable<IPAddressRange> {
    // The ranges are kept in order and non-overlapping
    List<IPAddressRange> m_ranges = new LinkedList<IPAddressRange>();
    
    public void add(IPAddressRange range) {
        IPAddressRange working = range;
        // we traverse the ordered list
        for(ListIterator<IPAddressRange> it = m_ranges.listIterator(); it.hasNext(); ) {
            IPAddressRange r = it.next();
            if (working.comesBefore(r) && !working.adjoins(r)) {
                // We've found the insertion point so just insert it and we are finished
                // go back one
                it.previous();
                // now insert it so it comes before the one we just returned
                it.add(working);
                // we have added the range so return
                return;
            } else if (working.combinable(r)) {
                working = working.combine(r);
                // now we remove the one we just merged since it is part of the working range
                it.remove();
            } else {
                // We have not yet found the insertion point
                //in which case we just go on to the next entry
            }
        }
        
        // if we got here then we have not yet added the working range
        // and it belongs at the end of the list
        m_ranges.add(working);
    }
    
    public void remove(IPAddressRange range) {
        for(ListIterator<IPAddressRange> it = m_ranges.listIterator(); it.hasNext(); ) {
            IPAddressRange r = it.next();
            IPAddressRange[] remaining = r.remove(range);
            // After removing a range form another there are 4 cases
            if (remaining.length == 0) {
                // 1. r is completely eclipse by range to there is nothing remaining
                // so we just remove it
                it.remove();
            } else if (remaining.length == 1) {
                // 2.  r overlaps the end of range so we have just the remaining end
                // so replace r with its left over end
                // 3. r and range do not over lap 
                // so we are just replacing r with itself
                it.set(remaining[0]);
            } else if (remaining.length == 2) {
                // 4. range is right in the middle of r so we have both ends or r
                // so we replace r with its right end and then add the left end
                it.set(remaining[0]);
                it.add(remaining[1]);
            }
        }            
    }
    
    public IPAddressRange[] toArray() {
        return m_ranges.toArray(new IPAddressRange[m_ranges.size()]);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("[");
        boolean first = true;
        for(IPAddressRange r : this) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(r);
        }
        buf.append("]");
        
        return buf.toString();
    }

    @Override
    public Iterator<IPAddressRange> iterator() {
        return Collections.unmodifiableList(m_ranges).iterator();
    }

    public void addAll(IPAddressRangeSet ranges) {
        for(IPAddressRange r : ranges) {
            add(r);
        }
    }
    
    public void removeAll(IPAddressRangeSet ranges) {
        for(IPAddressRange r : ranges) {
            remove(r);
        }
    }

    
}
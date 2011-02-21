/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class ConfigRangeList  {
    
    List<ConfigRange> m_ranges = new LinkedList<ConfigRange>();

    public void add(ConfigRange range) {
        ConfigRange working = range;
        for(ListIterator<ConfigRange> it = m_ranges.listIterator(); it.hasNext(); ) {
            ConfigRange r = it.next();
            if (working.preceeds(r) && !working.adjacent(r)) {
                // got back one
                it.previous();
                // not insert it so it comes before the one we just returned
                it.add(working);
                // we have added the range so return
                return;
            } else if (working.combinable(r)) {
                // we make a new range that combines the working range and the current one on the list
                working = working.combine(r);
                // now we remove the one we just merged since it is part of the working range
                it.remove();
            }
            // in which case we just go on to the next entry
        }
        
        // if we got here then we have not yet added the working range
        m_ranges.add(working);
    }
    
    public void remove(ConfigRange range) {
        for(ListIterator<ConfigRange> it = m_ranges.listIterator(); it.hasNext(); ) {
            ConfigRange r = it.next();
            ConfigRange[] remaining = r.remove(range);
            if (remaining.length == 0) {
                it.remove();
            } else if (remaining.length == 1) {
                it.set(remaining[0]);
            } else if (remaining.length == 2) {
                it.set(remaining[0]);
                it.add(remaining[1]);
            }
        }            
    }

    public ConfigRange[] toArray() {
        return m_ranges.toArray(new ConfigRange[m_ranges.size()]);
    }

    
}
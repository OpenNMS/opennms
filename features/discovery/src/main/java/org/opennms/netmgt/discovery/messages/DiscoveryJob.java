/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.IteratorIterator;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.base.Preconditions;

public class DiscoveryJob implements Serializable {
    private static final long serialVersionUID = 3750201609939695254L;

    private final List<IPPollRange> m_ranges;
    private final String m_foreignSource;
    private final String m_location;

    public DiscoveryJob(List<IPPollRange> ranges, String foreignSource, String location) {
        m_ranges = Preconditions.checkNotNull(ranges, "ranges argument");
        m_foreignSource = Preconditions.checkNotNull(foreignSource, "foreignSource argument");
        m_location = Preconditions.checkNotNull(location, "location argument");
    }

    public Iterable<IPPollAddress> getAddresses() {
        final List<Iterator<IPPollAddress>> iters = new ArrayList<Iterator<IPPollAddress>>();
        for(final IPPollRange range : m_ranges) {
            iters.add(range.iterator());
        }
        return new IteratorIterator<IPPollAddress>(iters);
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public String getLocation() {
        return m_location;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final DiscoveryJob other = (DiscoveryJob) obj;

       return com.google.common.base.Objects.equal(this.m_ranges, other.m_ranges) &&
               com.google.common.base.Objects.equal(this.m_foreignSource, other.m_foreignSource) &&
               com.google.common.base.Objects.equal(this.m_location, other.m_location);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(m_ranges, 
               m_foreignSource,
               m_location);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("foreignSource", m_foreignSource)
                 .add("location", m_location)
                 .add("ranges", m_ranges)
                 .toString();
    }
}

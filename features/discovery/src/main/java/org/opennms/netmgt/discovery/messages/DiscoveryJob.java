package org.opennms.netmgt.discovery.messages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.IteratorIterator;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.base.Preconditions;

public class DiscoveryJob {

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
                 .add("ranges", m_ranges)
                 .add("foreignSource", m_foreignSource)
                 .add("location", m_location)
                 .toString();
    }
}

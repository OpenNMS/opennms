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

package org.opennms.netmgt.discovery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.IteratorUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.base.Preconditions;

public class DiscoveryJob {

    private final List<IPPollRange> m_ranges;
    private final String m_foreignSource;
    private final String m_location;
    private final double m_packetsPerSecond;

    /**
     * Construct a {@link DiscoveryJob}. All ranges must have the 
     * same foreignSource and location for the message to be routed correctly.
     * 
     * @param ranges
     * @param foreignSource
     * @param location
     * @param packetsPerSecond
     */
    public DiscoveryJob(List<IPPollRange> ranges, String foreignSource, String location, double packetsPerSecond) {
        m_ranges = Preconditions.checkNotNull(ranges, "ranges argument");
        // NMS-8767: Allow null foreignSources so that Provisiond will create non-provisioned nodes
        //m_foreignSource = Preconditions.checkNotNull(foreignSource, "foreignSource argument");
        m_foreignSource = foreignSource;
        m_location = Preconditions.checkNotNull(location, "location argument");
        m_packetsPerSecond = packetsPerSecond > 0.0 ? packetsPerSecond : DiscoveryConfigFactory.DEFAULT_PACKETS_PER_SECOND;

        // Verify that all ranges in this job have the same foreign source
        Preconditions.checkState(m_ranges.stream().allMatch(range -> range.getForeignSource() == null || m_foreignSource.equals(range.getForeignSource())));
        // Verify that all ranges in this job have the same location
        Preconditions.checkState(m_ranges.stream().allMatch(range -> range.getLocation() == null || m_location.equals(range.getLocation())));
    }

    public List<IPPollRange> getRanges() {
        return m_ranges;
    }

    public Iterable<IPPollAddress> getAddresses() {
        final List<Iterator<IPPollAddress>> iters = new ArrayList<>();
        for(final IPPollRange range : m_ranges) {
            iters.add(range.iterator());
        }
        return IteratorUtils.concatIterators(iters);
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public String getLocation() {
        return m_location;
    }

    public double getPacketsPerSecond() {
        return m_packetsPerSecond;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DiscoveryJob)) {
            return false;
        }
        DiscoveryJob castOther = (DiscoveryJob) other;
        return Objects.equals(m_ranges, castOther.m_ranges)
                && Objects.equals(m_foreignSource, castOther.m_foreignSource)
                && Objects.equals(m_location, castOther.m_location)
                && Objects.equals(m_packetsPerSecond, castOther.m_packetsPerSecond);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_ranges, m_foreignSource, m_location, m_packetsPerSecond);
    }

    @Override
    public String toString() {
        return String.format("DiscoveryJob[foreignSource=%s, location=%s, packetPerSecond=%s, range=%s]",
                m_foreignSource, m_location, m_packetsPerSecond, m_ranges);
    }

}

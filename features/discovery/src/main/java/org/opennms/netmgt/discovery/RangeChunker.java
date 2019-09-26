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

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.opennms.core.network.IPAddress;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * <p>This class generates a list of {@link DiscoveryJob} instances that
 * are based on a "chunk" of a number of IP addresses that should be
 * polled as part of each job.</p>
 *
 * <ul>
 * <li>Input: {@link DiscoveryConfiguration}</li>
 * <li>Output: {@link List<DiscoveryJob>}</li>
 * </ul>
 */
public class RangeChunker {

    private final IpAddressFilter ipAddressFilter;

    public RangeChunker(IpAddressFilter ipAddressFilter) {
        this.ipAddressFilter = Objects.requireNonNull(ipAddressFilter);
    }

    public Map<String, List<DiscoveryJob>> chunk(final DiscoveryConfiguration config) {

        final int chunkSize = config.getChunkSize().orElse(DiscoveryConfigFactory.DEFAULT_CHUNK_SIZE);
        final double packetsPerSecond = config.getPacketsPerSecond().orElse(DiscoveryConfigFactory.DEFAULT_PACKETS_PER_SECOND);

        // If the foreign source for the discovery config is not set than use 
        // a value of null so that non-requisitioned nodes are created.
        //
        // TODO: Use the "default" foreign source instead so that we can move
        // away from using non-requisitioned nodes.
        //
        final String foreignSourceFromConfig = config.getForeignSource().isPresent()? config.getForeignSource().get().trim() : null;

        // If the monitoring location for the discovery config is not set than use 
        // the default localhost location
        final String locationFromConfig = config.getLocation().map(l -> {
            final String trimmed = l.trim();
            if ("".equals(trimmed)) {
                return null;
            }
            return trimmed;
        }).orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);

        final DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory(config);

        final AtomicReference<IPPollRange> previousRange = new AtomicReference<>();

        return StreamSupport.stream(configFactory.getConfiguredAddresses().spliterator(), false)
            .filter(address -> {
                // If there is no IP address filter set or the filter matches
                return ipAddressFilter.matches(address.getLocation(), address.getAddress());
            })
            // TODO: We could optimize this further by not unrolling IPPollRanges into individual
            // IPPollAddresses during the mapping.
            .map(address -> {
                // Create a singleton IPPollRange
                return new IPPollRange(
                    // Make sure that foreignSource is not null so that we can partition on the value
                    address.getForeignSource() == null ? foreignSourceFromConfig : address.getForeignSource(),
                    // Make sure that location is not null so that we can partition on the value
                    address.getLocation() == null ? locationFromConfig : address.getLocation(),
                    address.getAddress(),
                    address.getAddress(),
                    address.getTimeout(),
                    address.getRetries()
                );
            })
            .collect(Collectors.groupingBy(range -> {
                // Create a Map<ForeignSourceLocationKey,List<IPPollRange>>
                return new ForeignSourceLocationKey(
                    // Make sure that foreignSource is not null so that we can partition on the value
                    range.getForeignSource() == null ? foreignSourceFromConfig : range.getForeignSource(),
                    // Make sure that location is not null so that we can partition on the value
                    range.getLocation() == null ? locationFromConfig : range.getLocation()
                );
            }, LinkedHashMap::new, Collectors.toList()))
            .entrySet().stream()
            // Flat map one list of IPPollRanges to many chunked DiscoveryJobs
            .flatMap(entry -> {
                // Partition the list of address values
                return Lists.partition(entry.getValue(), chunkSize).stream()
                    // Map each partition value to a separate DiscoveryJob
                    .map(ranges -> {
                        DiscoveryJob retval = new DiscoveryJob(
                            ranges.stream()
                                .map(address -> {
                                    // If this address is consecutive with the previous range,
                                    // then just extend the range to cover this address too
                                    if (isConsecutive(previousRange.get(), address)) {
                                        previousRange.get().getAddressRange().incrementEnd();
                                        return null;
                                    }
                                    previousRange.set(address);
                                    return address;
                                })
                                // Filter out all of the consecutive values that we nulled out
                                .filter(Objects::nonNull)
                                // Convert back into a list of ranges
                                .collect(Collectors.toList()),
                            entry.getKey().getForeignSource(),
                            entry.getKey().getLocation(),
                            packetsPerSecond
                        );
                        // Reset the previousRange value
                        previousRange.set(null);
                        return retval;
                    })
                    // Collect the DiscoveryJobs
                    .collect(Collectors.toList()).stream();
            })
            .collect(Collectors.groupingBy(DiscoveryJob::getLocation,
                    LinkedHashMap::new, Collectors.toList()));
    }

    protected static boolean isConsecutive(IPPollRange range, IPPollRange address) {
        Preconditions.checkState(BigInteger.ONE.equals(address.getAddressRange().size()));
        return range != null && 
            new IPAddress(range.getAddressRange().getEnd()).isPredecessorOf(new IPAddress(address.getAddressRange().getEnd())) &&
            Objects.equals(range.getForeignSource(), address.getForeignSource()) &&
            Objects.equals(range.getLocation(), address.getLocation()) &&
            range.getRetries() == address.getRetries() &&
            range.getTimeout() == address.getTimeout();
    }

    private static class ForeignSourceLocationKey {
        private final String m_location;
        private final String m_foreignSource;

        public ForeignSourceLocationKey(String foreignSource, String location) {
            m_location = location;
            m_foreignSource = foreignSource;
        }

        public String getForeignSource() {
            return m_foreignSource;
        }

        public String getLocation() {
            return m_location;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ForeignSourceLocationKey other = (ForeignSourceLocationKey)obj;
            return Objects.equals(this.m_foreignSource, other.m_foreignSource)
                    && Objects.equals(this.m_location, other.m_location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(m_foreignSource, m_location);
        }
    }
}

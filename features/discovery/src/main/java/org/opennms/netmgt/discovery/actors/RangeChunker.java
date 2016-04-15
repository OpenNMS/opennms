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

package org.opennms.netmgt.discovery.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.discovery.IpAddressFilter;
import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.collect.Lists;

/**
 * <p>This class generates a list of {@link DiscoveryJob} instances that
 * are based on a "chunk" of a number of IP addresses that should be
 * polled as part of each job.</p>
 *
 * <ul>
 * <li>Input: {@link DiscoveryConfiguration}</li>
 * <li>Input: {@link List<DiscoveryJob>}</li>
 * </ul>
 */
public class RangeChunker
{
    private IpAddressFilter m_ipAddressFilter;

    public void setIpAddressFilter(IpAddressFilter ipAddressFilter) {
        m_ipAddressFilter = ipAddressFilter;
    }

    public List<DiscoveryJob> chunk( final DiscoveryConfiguration config )
    {
        int chunkSize = (config.getChunkSize() > 0) ? config.getChunkSize() : DiscoveryConfigFactory.DEFAULT_CHUNK_SIZE;
        double packetsPerSecond = (config.getPacketsPerSecond() > 0.0) ? config.getPacketsPerSecond() : DiscoveryConfigFactory.DEFAULT_PACKETS_PER_SECOND;

        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory( config );

        List<IPPollRange> ranges = new ArrayList<IPPollRange>();
        for ( IPPollAddress address : configFactory.getConfiguredAddresses() )
        {
            // If there is an IP address filter set
            if (m_ipAddressFilter != null) {
                // If the filter doesn't match the address
                if (!m_ipAddressFilter.matches(address.getAddress())) {
                    // Skip it
                    continue;
                }
            }

            IPPollRange range = new IPPollRange( address.getAddress(), address.getAddress(), address.getTimeout(),
                            address.getRetries() );
            ranges.add( range );
        }

        // If the foreign source for the discovery config is not set than use 
        // the default foreign source
        String foreignSource = (config.getForeignSource() == null || "".equals(config.getForeignSource().trim())) ? "default" : config.getForeignSource().trim();

        // If the monitoring location for the discovery config is not set than use 
        // the default localhost location
        String location = (config.getLocation() == null || "".equals(config.getLocation().trim())) ? MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID : config.getLocation().trim();

        return Lists.partition( ranges, chunkSize ).stream().map(
                        r -> new DiscoveryJob( new ArrayList<IPPollRange>( r ), foreignSource,
                                        location, packetsPerSecond ) ).collect( Collectors.toList() );

    }
}

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
import org.opennms.netmgt.discovery.messages.DiscoveryJob;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

import com.google.common.collect.Lists;

public class RangeChunker
{
    public static final int DEFAULT_CHUNK_SIZE = 100;

    public List<DiscoveryJob> chunk( DiscoveryConfiguration config )
    {
        int chunkSize = (config.getChunkSize() > 0) ? config.getChunkSize() : DEFAULT_CHUNK_SIZE;
        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory( config );

        List<IPPollRange> ranges = new ArrayList<IPPollRange>();
        for ( IPPollAddress address : configFactory.getConfiguredAddresses() )
        {
            IPPollRange range = new IPPollRange( address.getAddress(), address.getAddress(), address.getTimeout(),
                            address.getRetries() );
            ranges.add( range );
        }

        return Lists.partition( ranges, chunkSize ).stream().map(
                        r -> new DiscoveryJob( new ArrayList<IPPollRange>( r ), config.getForeignSource(),
                                        config.getLocation() ) ).collect( Collectors.toList() );

    }
}

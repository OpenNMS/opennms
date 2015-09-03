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
                        r -> new DiscoveryJob( r, config.getForeignSource(), config.getLocation() ) ).collect(
                                        Collectors.toList() );

    }
}

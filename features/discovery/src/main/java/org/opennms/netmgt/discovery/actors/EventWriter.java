package org.opennms.netmgt.discovery.actors;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class EventWriter
{
    private static final Logger LOG = LoggerFactory.getLogger( EventWriter.class );

    private final EventIpcManager m_ipc_manager;

    public EventWriter ( EventIpcManager ipcManager )
    {
        m_ipc_manager = Preconditions.checkNotNull( ipcManager, "ipcManager argument" );
    }

    public void sendEvents( DiscoveryResults results )
    {
        results.getResponses().entrySet().forEach(
                        e -> sendNewSuspectEvent( e.getKey(), e.getValue(), results.getForeignSource() ) );
    }

    private void sendNewSuspectEvent( InetAddress address, Long rtt, String foreignSource )
    {
        EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
        eb.setInterface( address );
        eb.setHost( InetAddressUtils.getLocalHostName() );

        eb.addParam( "RTT", rtt );

        if ( foreignSource != null )
        {
            eb.addParam( "foreignSource", foreignSource );
        }

        try
        {
            m_ipc_manager.sendNow( eb.getEvent() );
            LOG.debug( "Sent event: {}", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI );
        }
        catch ( Throwable t )
        {
            LOG.warn( "run: unexpected throwable exception caught during send to middleware", t );
        }
    }
}

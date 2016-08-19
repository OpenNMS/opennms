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

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class EventWriter
{
    private static final Logger LOG = LoggerFactory.getLogger( EventWriter.class );

    private final EventForwarder m_ipc_manager;

    public EventWriter ( EventForwarder ipcManager )
    {
        m_ipc_manager = Preconditions.checkNotNull( ipcManager, "ipcManager argument" );
    }

    public void sendEvents( DiscoveryResults results ) {
        results.getResponses().entrySet().forEach(
                        e -> sendNewSuspectEvent( e.getKey(), e.getValue(),
                                ImmutableMap.of(
                                        "foreignSource", results.getForeignSource(),
                                        "location", results.getLocation())));
    }

    private void sendNewSuspectEvent( InetAddress address, Long rtt, Map<String, String> eventParameters) {
        EventBuilder eb = new EventBuilder( EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "OpenNMS.Discovery" );
        eb.setInterface( address );
        eb.setHost( InetAddressUtils.getLocalHostName() );
        eb.addParam( "RTT", rtt );

        eventParameters.entrySet().forEach(eachEntry -> {
            if (eachEntry.getValue() != null) {
                eb.addParam( eachEntry.getKey(), eachEntry.getValue());
            }
        });

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

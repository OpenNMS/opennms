/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.ticketing.inmemory;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;


/**
 * This Plugin is used to test ticketing workflow.
 * Need to add below system properties.
 * opennms.ticketer.plugin=org.opennms.netmgt.ticketd.OSGiBasedTicketerPlugin
   opennms.alarmTroubleTicketEnabled = true
    and enable opennms-inmemory-ticketer feature.
 */
public class InMemoryTicketerPlugin implements Plugin {

    private final TicketMapper ticketMapper;

    private final AtomicInteger ticketIdGenerator = new AtomicInteger(0);

    public InMemoryTicketerPlugin(TicketMapper ticketMapper) {
        this.ticketMapper = ticketMapper;
    }

    @Override
    public Ticket get(String ticketId) throws PluginException {
        return ticketMapper.getTicket(ticketId);
    }

    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {
        if (ticket.getId() == null) {
            Integer ticketId = ticketIdGenerator.incrementAndGet();
            ticket.setId(ticketId.toString());
        }
        ticketMapper.updateTicket(ticket);
    }
}

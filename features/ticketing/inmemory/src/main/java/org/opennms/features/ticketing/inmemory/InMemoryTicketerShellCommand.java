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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.api.integration.ticketing.Ticket;

@Command(scope = "opennms", name = "inmemory-ticketer", description = "Queries/Update tickets from InMemoryTicketer")
@Service
public class InMemoryTicketerShellCommand implements Action {

    @Reference
    private TicketMapper ticketMapper;

    @Option(name = "-t", aliases = "--ticketId", description = "Specify ticketId", required = true)
    private String ticketId;

    @Argument(index = 0 , name = "setState", description = "Set Ticket state")
    @Completion(value = TicketStateNameCompleter.class, caseSensitive = false)
    private Ticket.State ticketState;

    @Override
    public Object execute() throws Exception {

        if (ticketId == null) {
            throw new IllegalArgumentException("ticketId required");
        }
        Ticket ticket = ticketMapper.getTicket(ticketId);
        if (ticket == null) {
            System.out.printf("Not able to find ticket with id %s in InMemoryTicketer \n ", ticketId);
            return null;
        } else if (ticketState != null) {
            ticket.setState(ticketState);
            ticketMapper.updateTicket(ticket);
            System.out.printf("Updated ticket with state : %s \n", ticketState.name());
        }
        System.out.printf("Ticket Details for ticketId %s :\n", ticketId);
        System.out.println(ticket);
        return null;
    }
}

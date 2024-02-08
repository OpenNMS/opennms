/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

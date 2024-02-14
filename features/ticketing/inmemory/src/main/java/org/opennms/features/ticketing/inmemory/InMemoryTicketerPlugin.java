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

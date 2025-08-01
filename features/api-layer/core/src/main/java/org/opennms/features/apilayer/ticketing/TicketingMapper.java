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
package org.opennms.features.apilayer.ticketing;

import org.opennms.integration.api.v1.ticketing.immutables.ImmutableTicket;
import org.opennms.integration.api.v1.ticketing.Ticket;

import com.google.common.base.Enums;

/**
 *  Helper Class to Map @{@link Ticket} to {@link org.opennms.api.integration.ticketing.Ticket} and viceversa.
 */
public class TicketingMapper {

    public static Ticket buildTicket(org.opennms.api.integration.ticketing.Ticket ticket) {
        return ImmutableTicket.newBuilder()
                .setAlarmId(ticket.getAlarmId())
                .setAttributes(ticket.getAttributes())
                .setDetails(ticket.getDetails())
                .setId(ticket.getId())
                .setIpAddress(ticket.getIpAddress())
                .setNodeId(ticket.getNodeId())
                .setState(Ticket.State.valueOf(ticket.getState().name()))
                .setSummary(ticket.getSummary())
                .build();
    }

    public static org.opennms.api.integration.ticketing.Ticket buildTicket(Ticket ticket) {
        org.opennms.api.integration.ticketing.Ticket newTicket = new org.opennms.api.integration.ticketing.Ticket();
        newTicket.setAlarmId(ticket.getAlarmId());
        newTicket.setAttributes(ticket.getAttributes());
        newTicket.setDetails(ticket.getDetails());
        newTicket.setId(ticket.getId());
        newTicket.setIpAddress(ticket.getIpAddress());
        newTicket.setNodeId(ticket.getNodeId());
        newTicket.setState(Enums.getIfPresent(org.opennms.api.integration.ticketing.Ticket.State.class,
                ticket.getState().name()).or(org.opennms.api.integration.ticketing.Ticket.State.OPEN));
        newTicket.setSummary(ticket.getSummary());
        newTicket.setUser(ticket.getUser());
        return newTicket;
    }
}

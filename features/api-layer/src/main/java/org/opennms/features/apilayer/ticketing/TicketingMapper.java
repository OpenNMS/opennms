/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

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

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.integration.api.v1.ticketing.Ticket;
import org.opennms.integration.api.v1.ticketing.TicketingPlugin;
import org.osgi.framework.BundleContext;

/**
 * Manager that registers @{@link TicketingPlugin} from Integration API to OpenNMS.
 */
public class TicketingPluginManager extends InterfaceMapper<TicketingPlugin, Plugin> {

    public TicketingPluginManager(BundleContext bundleContext) {
        super(Plugin.class, bundleContext);
    }

    @Override
    public Plugin map(TicketingPlugin ext) {
        return new Plugin() {
            @Override
            public org.opennms.api.integration.ticketing.Ticket get(String ticketId) throws PluginException {
                try {
                    Ticket ticket = ext.get(ticketId);
                    if (ticket != null) {
                        return TicketingMapper.buildTicket(ticket);
                    }
                    throw new PluginException("Failed to find ticket with id : " + ticketId);

                } catch (Exception e) {
                    throw new PluginException(e);
                }
            }

            @Override
            public void saveOrUpdate(org.opennms.api.integration.ticketing.Ticket ticket) throws PluginException {
                try {
                    Ticket retrieved = TicketingMapper.buildTicket(ticket);
                    String ticketId = ext.saveOrUpdate(retrieved);
                    if(ticketId != null) {
                        ticket.setId(ticketId);
                    }
                } catch (Exception e) {
                    throw new PluginException(e);
                }
            }
        };
    }
}

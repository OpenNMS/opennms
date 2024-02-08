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

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.features.apilayer.common.utils.InterfaceMapper;
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

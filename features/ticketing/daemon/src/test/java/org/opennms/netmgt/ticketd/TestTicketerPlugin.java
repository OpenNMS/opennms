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
package org.opennms.netmgt.ticketd;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;

public class TestTicketerPlugin implements Plugin {

    private Ticket m_ticket;

    @Override
    public Ticket get(String ticketId) throws PluginException {
        if (ticketId.equals("testId")) {
            return m_ticket;
        }
        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        return ticket;
    }

    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {
        m_ticket = ticket;
        m_ticket.setId("testId");
    }
}

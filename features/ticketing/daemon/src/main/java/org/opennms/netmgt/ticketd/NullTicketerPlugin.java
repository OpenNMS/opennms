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

import org.opennms.api.integration.ticketing.*;

/**
 * OpenNMS Trouble Ticket Plugin API implementation used as a no-op when not custom
 * tickeing plugin is implemented... Allows the daemon to be started without having
 * to have actual implementation or configuration.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class NullTicketerPlugin implements Plugin {

    /** {@inheritDoc} */
    @Override
    public Ticket get(String ticketId) {
        Ticket ticket = new Ticket();
        ticket.setId("Ticketing not configured");
        return ticket;
    }

    /**
     * {@inheritDoc}
     *
     * No-op implementation
     */
    @Override
    public void saveOrUpdate(Ticket ticket) {
    }

}

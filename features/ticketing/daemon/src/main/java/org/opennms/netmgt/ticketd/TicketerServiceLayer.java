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

import java.util.Map;

import org.opennms.api.integration.ticketing.Plugin;
import org.springframework.transaction.annotation.Transactional;

/**
 * OpenNMS Trouble Ticket API
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@Transactional
public interface TicketerServiceLayer {
	
	/**
	 * Implement to manage creation of tickets through registered plugin.
	 *
	 * @param alarmId a int.
	 */
	public void createTicketForAlarm(int alarmId, Map<String,String> attributes);
	
	/**
	 * Implement to manage updating of tickets through registered plugin.
	 *
	 * @param alarmId a int.
	 * @param ticketId a {@link java.lang.String} object.
	 */
	public void updateTicketForAlarm(int alarmId, String ticketId);
	
	/**
	 * Implement to manage closing of tickets through registered plugin.
	 *
	 * @param alarmId a int.
	 * @param ticketId a {@link java.lang.String} object.
	 */
	public void closeTicketForAlarm(int alarmId, String ticketId);
	
	/**
	 * Implement to manage canceling of tickets through registered plugin.
	 *
	 * @param alarmId a int.
	 * @param ticketId a {@link java.lang.String} object.
	 */
	public void cancelTicketForAlarm(int alarmId, String ticketId);
    
	/**
	 * Implement to reload ticketer when requested.
	 *
	 */
	public void reloadTicketer();

	/**
	 * Set the ticketer plugin.
	 *
	 */
	public void setTicketerPlugin(Plugin ticketerPlugin);
}

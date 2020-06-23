/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.scriptd.helper;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * Interface to use for triggering
 * a generic event synchronization
 *  
 * @author antonio
 *
 */
public interface EventSynchronization {

	/**
	 * This method just add an event forwarder
	 * to forward sync events
	 * 
	 * @param forwarder
	 */

	void addEventForwarder(EventForwarder forwarder);
	
	/**
	 * 
	 * Calling this method get the synchronization
	 * Events 
	 * 
	 */
	List<Event> getEvents();

	/**
	 * 
	 * this method sync
	 * Events 
	 * 
	 */
	void sync();

}

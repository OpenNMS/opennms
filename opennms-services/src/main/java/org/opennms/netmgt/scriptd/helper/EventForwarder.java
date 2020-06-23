/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

/**
 * Interface to forward events.
 * 
 * @author antonio
 *
 */

public interface EventForwarder {
	
	/**
	 * Method to add a policy rule
	 * to match event to be forwarded or dropped
	 * @param filter
	 */
	public void setEventPolicyRule(EventPolicyRule filter);	

	/**
	 * 
	 * Method used to flush Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushEvent(Event event);

	/**
	 * 
	 * Method used to flush Sync Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushSyncEvent(Event event);

	 /**
	  * This method should be invoked before
	  * flushing sync events
	  * The class implementation should
	  * send the "startSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendStartSync();
	 
	 /**
	  * This method should be invoked after
	  * flushing sync events.
	  * The class implementation should 
	  * send the "endSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendEndSync();
}

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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;
/**
 * An EventFilter is a filter of Events
 * An implementation of this interface is a class
 * where you have some criteria to decide if the Event
 * pass the filter or not
 * 
 * @author antonio
 *
 */
public interface EventPolicyRule {

	List<EventMatch> m_filter = new ArrayList<>();
	List<Boolean> m_forwardes = new ArrayList<>();

	/**
	 * 
	 * Method to decide if the event 
	 * should be forwarder
	 * 
	 * @return event
	 * the filtered Event
	 * that can be null or 
	 * with parameter changes
	 * 
	 */

	Event filter(Event event);
	
	void addForwardRule(EventMatch eventMatch);
	
	void addDropRule(EventMatch eventMatch);

}

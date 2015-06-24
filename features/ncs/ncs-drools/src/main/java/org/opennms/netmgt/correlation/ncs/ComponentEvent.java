/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.ncs;

import org.opennms.netmgt.xml.event.Event;

public class ComponentEvent {

	protected Component m_component;
	protected Event m_event;
	
	protected ComponentEvent(Component component, Event event) {
		m_component = component;
		m_event = event;
	}

	public Component getComponent() {
	    return m_component;
	}

	public void setComponent(Component component) {
	    m_component = component;
	}

	public Event getEvent() {
		return m_event;
	}

	public void setEvent(Event event) {
		m_event = event;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" +
				"component=" + m_component + 
				", event=" + m_event.getUei() + "(" + m_event.getDbid() + ")" +
				"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_component == null) ? 0 : m_component.hashCode());
		//result = prime * result + ((m_event == null) ? 0 : m_event.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ComponentEvent other = (ComponentEvent) obj;
		if (m_component == null) {
			if (other.m_component != null)
				return false;
		} else if (!m_component.equals(other.m_component))
			return false;
		
//		if (m_event == null) {
//			if (other.m_event != null)
//				return false;
//		} else if (!m_event.equals(other.m_event))
//			return false;

		return true;
	}

}

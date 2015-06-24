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

import static org.opennms.netmgt.correlation.ncs.Utils.nullSafeEquals;

import org.opennms.netmgt.xml.event.Event;

public class Impacted {

	private Component m_target;
	private Event m_cause;
	
	public Impacted() {}
	
	public Impacted(Component target, Event cause)
	{
		m_target = target;
		m_cause = cause;
	}
	
	public Component getTarget() {
		return m_target;
	}
	public void setTarget(Component target) {
		m_target = target;
	}
	public Event getCause() {
		return m_cause;
	}
	public void setCause(Event cause) {
		m_cause = cause;
	}

	@Override
	public String toString() {
		return "Impacted[ target=" + m_target + 
				", cause=" + m_cause.getUei() + "(" + m_cause.getDbid() + ")" +
				" ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_cause == null) ? 0 : m_cause.hashCode());
		result = prime * result
				+ ((m_target == null) ? 0 : m_target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj instanceof Impacted) {
			Impacted o = (Impacted)obj;
			return nullSafeEquals(m_target, o.m_target)
				&& m_cause == null 
				? o.m_cause == null
				: o.m_cause == null
				? false
			    : m_cause.getDbid() == null
			    ? o.m_cause.getDbid() == null
			    : o.m_cause.getDbid() == null
			    ? false
			    : m_cause.getDbid().equals(o.m_cause.getDbid());
		}
		return false;
	}
	
	
	
	

}
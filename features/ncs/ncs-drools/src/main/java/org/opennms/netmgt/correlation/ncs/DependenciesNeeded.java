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

public class DependenciesNeeded {
	
	private Component m_component;
	private Object m_requestor;
	
	public DependenciesNeeded(Component component, Object requestor) {
		m_component = component;
		m_requestor = requestor;
	}

	public Component getComponent() {
		return m_component;
	}

	public void setComponent(Component component) {
		m_component = component;
	}

	public Object getRequestor() {
		return m_requestor;
	}

	public void setRequestor(Object requestor) {
		m_requestor = requestor;
	}

	@Override
	public String toString() {
		return "DependenciesNeeded [component=" + m_component
				+ ", requestor=" + m_requestor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_component == null) ? 0 : m_component.hashCode());
		result = prime * result
				+ ((m_requestor == null) ? 0 : m_requestor.hashCode());
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
		DependenciesNeeded other = (DependenciesNeeded) obj;
		if (m_component == null) {
			if (other.m_component != null)
				return false;
		} else if (!m_component.equals(other.m_component))
			return false;
		if (m_requestor == null) {
			if (other.m_requestor != null)
				return false;
		} else if (!m_requestor.equals(other.m_requestor))
			return false;
		return true;
	}
	
	
	
	

}

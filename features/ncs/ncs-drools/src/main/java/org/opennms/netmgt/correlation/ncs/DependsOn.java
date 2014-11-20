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

public class DependsOn {
	private Component m_a;
	private Component m_b;
	
	public DependsOn() {}
	
	public DependsOn(Component a, Component b)
	{
		m_a = a;
		m_b = b;
	}

	public Component getA() {
		return m_a;
	}

	public void setA(Component a) {
		m_a = a;
	}

	public Component getB() {
		return m_b;
	}

	public void setB(Component b) {
		m_b = b;
	}
	
	@Override
	public String toString() {
		return "DependsOn[ a=" + m_a + ", b=" + m_b + " ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_a == null) ? 0 : m_a.hashCode());
		result = prime * result + ((m_b == null) ? 0 : m_b.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj instanceof DependsOn) {
			DependsOn d = (DependsOn)obj;
			
			return nullSafeEquals(m_a, d.m_a)
				&& nullSafeEquals(m_b, d.m_b);
		}
		return false;
	}
	
	
}

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

import java.util.List;



public class DependsOnAny {
	private Component m_component;
	private List<Component> m_subComponents;
	
	public DependsOnAny() {}
	
	public DependsOnAny(Component component, List<Component> subComponents)
	{
		m_component = component;
		m_subComponents = subComponents;
	}

    public Component getComponent() {
        return m_component;
    }

    public void setComponent(Component component) {
        m_component = component;
    }

    public List<Component> getSubComponents() {
        return m_subComponents;
    }

    public void setSubComponents(List<Component> subComponents) {
        m_subComponents = subComponents;
    }

	
	
	
	
	
}

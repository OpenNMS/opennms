/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.ncs;

import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponent.NodeIdentification;

public class NCSBuilder {
	
	final private NCSBuilder m_parent;
	final private NCSComponent m_component;
	
	public NCSBuilder(String type, String foreignSource, String foreignId) {
		this(null, new NCSComponent(type, foreignSource, foreignId));
	}
	
	public NCSBuilder(NCSBuilder parent, NCSComponent component) {
		m_parent = parent;
		m_component = component;
	}
	
	public NCSBuilder setForeignSource(String foreignSource) {
		m_component.setForeignSource(foreignSource);
		return this;
	}
	
	public NCSBuilder setForeignId(String foreignId) {
		m_component.setForeignId(foreignId);
		return this;
	}
	
	public NCSBuilder setNodeIdentity(String nodeForeignSource, String nodeForeignId) {
		m_component.setNodeIdentification(new NodeIdentification(nodeForeignSource, nodeForeignId));
		return this;
	}
	
	public NCSBuilder setType(String type) {
		m_component.setType(type);
		return this;
	}
	
	public NCSBuilder setName(String name) {
		m_component.setName(name);
		return this;
	}
	
	public NCSBuilder setUpEventUei(String upEventUei) {
		m_component.setUpEventUei(upEventUei);
		return this;
	}

	public NCSBuilder setDownEventUei(String downEventUei) {
		m_component.setDownEventUei(downEventUei);
		return this;
	}

	public NCSBuilder setAttribute(String key, String value) {
		m_component.setAttribute(key, value);
		return this;
	}
	
	public NCSBuilder setDependenciesRequired(DependencyRequirements requirements)
	{
		m_component.setDependenciesRequired(requirements);
		return this;
	}

	public NCSBuilder pushComponent(String type, String foreignSource, String foreignId) {
		NCSComponent sub = new NCSComponent(type, foreignSource, foreignId);
		m_component.addSubcomponent(sub);
		return new NCSBuilder(this, sub);
	}
	
	public NCSBuilder popComponent() {
		return m_parent;
	}
	
	public NCSComponent get() {
		return m_component;
	}

}

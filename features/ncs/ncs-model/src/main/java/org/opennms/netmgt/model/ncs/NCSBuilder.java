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

package org.opennms.netmgt.model.ncs;

import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponent.NodeIdentification;

public class NCSBuilder {
	
	private final NCSBuilder m_parent;
	private final NCSComponent m_component;
	
	/**
	 * @param type
	 * @param foreignSource
	 * @param foreignId
	 */
	public NCSBuilder(String type, String foreignSource, String foreignId) {
		this(null, new NCSComponent(type, foreignSource, foreignId));
	}
	
	/**
	 * @param parent
	 * @param component
	 */
	public NCSBuilder(NCSBuilder parent, NCSComponent component) {
		m_parent = parent;
		m_component = component;
	}
	
	/**
	 * @param foreignSource
	 */
	public NCSBuilder setForeignSource(String foreignSource) {
		m_component.setForeignSource(foreignSource);
		return this;
	}
	
	/**
	 * @param foreignId
	 */
	public NCSBuilder setForeignId(String foreignId) {
		m_component.setForeignId(foreignId);
		return this;
	}
	
	/**
	 * @param nodeForeignSource
	 * @param nodeForeignId
	 */
	public NCSBuilder setNodeIdentity(String nodeForeignSource, String nodeForeignId) {
		m_component.setNodeIdentification(new NodeIdentification(nodeForeignSource, nodeForeignId));
		return this;
	}
	
	/**
	 * @param type
	 */
	public NCSBuilder setType(String type) {
		m_component.setType(type);
		return this;
	}
	
	/**
	 * @param name
	 */
	public NCSBuilder setName(String name) {
		m_component.setName(name);
		return this;
	}
	
	/**
	 * @param upEventUei
	 */
	public NCSBuilder setUpEventUei(String upEventUei) {
		m_component.setUpEventUei(upEventUei);
		return this;
	}

	/**
	 * @param downEventUei
	 */
	public NCSBuilder setDownEventUei(String downEventUei) {
		m_component.setDownEventUei(downEventUei);
		return this;
	}

	/**
	 * @param key
	 * @param value
	 */
	public NCSBuilder setAttribute(String key, String value) {
		m_component.setAttribute(key, value);
		return this;
	}
	
	/**
	 * @param requirements
	 */
	public NCSBuilder setDependenciesRequired(DependencyRequirements requirements)
	{
		m_component.setDependenciesRequired(requirements);
		return this;
	}

	/**
	 * @param type
	 * @param foreignSource
	 * @param foreignId
	 */
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

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

package org.opennms.features.vaadin.topology;

public class Group {
	
	private int m_level;
	

	private Group m_parent;
	public static final Group ROOT = new Group(0);
	
	public Group(int level) {
		this(level, null);
	}
	
	public Group(int level, Group parent) {
		if(level <= parent.getLevel()) {
			throw new IllegalArgumentException("A group must have a level greater than its parents");
		}
		setLevel(level);
		setParent(parent);
	}

	public int getLevel() {
		return m_level;
	}

	public void setLevel(int level) {
		m_level = level;
	}
	public Group getParent() {
		return m_parent;
	}

	public void setParent(Group parent) {
		m_parent = parent;
	}
	
}

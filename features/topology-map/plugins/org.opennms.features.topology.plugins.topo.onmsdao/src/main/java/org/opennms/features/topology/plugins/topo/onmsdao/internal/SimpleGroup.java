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

package org.opennms.features.topology.plugins.topo.onmsdao.internal;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroup extends SimpleVertex {

	List<SimpleVertex> m_members = new ArrayList<SimpleVertex>();
	
	int m_mapid;
	public SimpleGroup() {}
	
	public SimpleGroup(int mapid, String groupId) {
	    super(groupId);
	    m_mapid = mapid;
	}

        public int getMapid() {
            return m_mapid;
        }

        public void setMapid(int mapid) {
            m_mapid = mapid;
        }

        @Override
	public boolean isLeaf() {
		return false;
	}
	
	public List<SimpleVertex> getMembers() {
		return m_members;
	}
	
	public void addMember(SimpleVertex v) {
		m_members.add(v);
	}
	
	public void removeMember(SimpleVertex v) {
		m_members.remove(v);
	}

}

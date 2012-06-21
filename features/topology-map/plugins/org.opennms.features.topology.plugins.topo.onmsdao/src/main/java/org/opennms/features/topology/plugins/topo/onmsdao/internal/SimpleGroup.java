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

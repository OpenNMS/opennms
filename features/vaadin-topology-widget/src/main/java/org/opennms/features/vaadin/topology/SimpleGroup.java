package org.opennms.features.vaadin.topology;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroup extends SimpleVertex {

	List<SimpleVertex> m_members = new ArrayList<SimpleVertex>();
	
	public SimpleGroup(String id) {
		super(id);
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

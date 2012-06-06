package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="group")
public class SimpleGroup extends GVertex {

	List<GVertex> m_members = new ArrayList<GVertex>();
	
	
	public SimpleGroup() {}
	
	public SimpleGroup(String id) {
		super(id);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@XmlIDREF
	public List<GVertex> getMembers() {
		return m_members;
	}
	
	public void addMember(GVertex v) {
		m_members.add(v);
	}
	
	public void removeMember(GVertex v) {
		m_members.remove(v);
	}

}

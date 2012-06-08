package org.opennms.features.topology.plugins.topo.simple.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="group")
public class SimpleGroup extends SimpleVertex {

	List<SimpleVertex> m_members = new ArrayList<SimpleVertex>();
	
	
	public SimpleGroup() {}
	
	public SimpleGroup(String id) {
		super(id);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@XmlIDREF
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

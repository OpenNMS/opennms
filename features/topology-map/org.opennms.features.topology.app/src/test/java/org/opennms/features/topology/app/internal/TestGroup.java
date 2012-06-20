package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="group")
public class TestGroup extends TestVertex {

	List<TestVertex> m_members = new ArrayList<TestVertex>();
	
	
	public TestGroup() {}
	
	public TestGroup(String id) {
		super(id);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@XmlIDREF
	public List<TestVertex> getMembers() {
		return m_members;
	}
	
	public void addMember(TestVertex v) {
		m_members.add(v);
	}
	
	public void removeMember(TestVertex v) {
		m_members.remove(v);
	}

}

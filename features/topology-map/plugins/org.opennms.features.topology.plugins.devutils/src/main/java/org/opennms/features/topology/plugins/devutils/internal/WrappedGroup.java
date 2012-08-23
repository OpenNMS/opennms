package org.opennms.features.topology.plugins.devutils.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import com.vaadin.data.Item;

@XmlRootElement(name="group")
public class WrappedGroup extends WrappedVertex {

	List<WrappedVertex> m_members = new ArrayList<WrappedVertex>();
	
	
	public WrappedGroup() {}
	
	public WrappedGroup(Item group) {
		super(group);
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@XmlIDREF
	public List<WrappedVertex> getMembers() {
		return m_members;
	}
	
	public void addMember(WrappedVertex v) {
		m_members.add(v);
	}
	
	public void removeMember(WrappedVertex v) {
		m_members.remove(v);
	}

}

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.GROUP_ICON;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="group")
public class LinkdGroup extends LinkdVertex {

	List<LinkdVertex> m_members = new ArrayList<LinkdVertex>();
	
	int m_mapid;
	public LinkdGroup() {}
	
	public LinkdGroup(String groupId) {
	    super(groupId, GROUP_ICON);
	}

        @Override
	public boolean isLeaf() {
		return false;
	}
	
        @XmlIDREF
        public List<LinkdVertex> getMembers() {
		return m_members;
	}
	
	public void addMember(LinkdVertex v) {
		m_members.add(v);
	}
	
	public void removeMember(LinkdVertex v) {
		m_members.remove(v);
	}

}

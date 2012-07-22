package org.opennms.features.topology.plugins.topo.linkd.internal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="vertex")
public class LinkdNodeVertex extends LinkdVertex {

	public LinkdNodeVertex() {}
	
	int m_nodeid;
	
        public LinkdNodeVertex(String id, int x, int y, String icon, String label) {
            super(id, x, y, icon, label);
            m_nodeid=Integer.parseInt(id);
        }

        public int getNodeid() {
            return m_nodeid;
        }

        @Override
	public boolean isLeaf() {
		return true;
	}
	

}

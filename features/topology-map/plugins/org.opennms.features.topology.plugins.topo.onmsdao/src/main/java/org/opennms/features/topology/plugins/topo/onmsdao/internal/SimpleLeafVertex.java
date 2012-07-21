package org.opennms.features.topology.plugins.topo.onmsdao.internal;


public class SimpleLeafVertex extends SimpleVertex {

	public SimpleLeafVertex() {}
	
	int m_nodeid;
	
        public SimpleLeafVertex(int nodeid, String id, int x, int y) {
            super(id, x, y);
            m_nodeid=nodeid;
        }

        public int getNodeid() {
            return m_nodeid;
        }

        @Override
	public boolean isLeaf() {
		return true;
	}
	

}

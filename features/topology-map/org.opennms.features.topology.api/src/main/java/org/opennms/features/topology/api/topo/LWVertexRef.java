package org.opennms.features.topology.api.topo;

public class LWVertexRef extends LWRef implements VertexRef {

	public LWVertexRef(VertexRef ref) {
		super(ref);
	}

	public LWVertexRef(String namespace, String id) {
		super(namespace, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VertexRef) {
			return super.equals(obj);
		}
		return false;
	}

	
	@Override
	public String toString() { return "VertexRef:"+getNamespace()+":"+getId(); } 

}

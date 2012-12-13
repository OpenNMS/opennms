package org.opennms.features.topology.api.topo;

public class LWEdgeRef extends LWRef implements EdgeRef {

	public LWEdgeRef(EdgeRef ref) {
		super(ref);
	}

	public LWEdgeRef(String namespace, String id) {
		super(namespace, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EdgeRef) {
			return super.equals(obj);
		}
		return false;
	}

	@Override
	public String toString() { return "EdgeRef:"+getNamespace()+":"+getId(); } 
}

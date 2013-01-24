package org.opennms.features.topology.api.topo;

class LWRef implements Ref {
	
	private String m_namespace;
	private String m_id;
	
	protected LWRef(String namespace, String id) {
		m_namespace = namespace;
		m_id = id;
	}
	
	protected LWRef(Ref ref) {
		this(ref.getNamespace(), ref.getId());
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public String getNamespace() {
		return m_namespace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		result = prime * result
				+ ((m_namespace == null) ? 0 : m_namespace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (!(obj instanceof Ref)) return false;

		Ref ref = (Ref)obj;
		
		return getNamespace().equals(ref.getNamespace()) && getId().equals(ref.getId());

	}

}

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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LWRef other = (LWRef) obj;
		if (m_id == null) {
			if (other.m_id != null)
				return false;
		} else if (!m_id.equals(other.m_id))
			return false;
		if (m_namespace == null) {
			if (other.m_namespace != null)
				return false;
		} else if (!m_namespace.equals(other.m_namespace))
			return false;
		return true;
	}

}

package org.opennms.netmgt.ncs.persistence;

import org.apache.commons.lang.builder.CompareToBuilder;

public final class ComponentIdentifier implements Comparable<ComponentIdentifier> {
	private final String m_type;
	private final String m_name;
	private final String m_foreignSource;
	private final String m_foreignId;

	public ComponentIdentifier(final String type, final String foreignSource, final String foreignId) {
		m_type          = type;
		m_name          = null;
		m_foreignSource = foreignSource;
		m_foreignId     = foreignId;
	}

	public ComponentIdentifier(final String type, final String foreignSource, final String foreignId, final String name) {
		m_type          = type;
		m_name          = name;
		m_foreignSource = foreignSource;
		m_foreignId     = foreignId;
	}

	public String getType()          { return m_type; }
	public String getName()          { return m_name; }
	public String getForeignSource() { return m_foreignSource; }
	public String getForeignId()     { return m_foreignId; }

	@Override
	public String toString() {
		return "ComponentIdentifier[" + m_type + "|" + m_name + "|" + m_foreignSource + "|" + m_foreignId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_foreignId == null) ? 0 : m_foreignId.hashCode());
		result = prime * result + ((m_foreignSource == null) ? 0 : m_foreignSource.hashCode());
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ComponentIdentifier)) return false;
		final ComponentIdentifier other = (ComponentIdentifier) obj;
		if (m_foreignId == null) {
			if (other.m_foreignId != null) return false;
		} else if (!m_foreignId.equals(other.m_foreignId)) {
			return false;
		}
		if (m_foreignSource == null) {
			if (other.m_foreignSource != null) return false;
		} else if (!m_foreignSource.equals(other.m_foreignSource)) {
			return false;
		}
		if (m_type == null) {
			if (other.m_type != null) return false;
		} else if (!m_type.equals(other.m_type)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(final ComponentIdentifier o) {
		return new CompareToBuilder()
			.append(m_foreignId, o.getForeignId())
			.append(m_foreignSource, o.getForeignSource())
			.append(m_type, o.getType())
			.append(m_name, o.getName())
			.toComparison();
	}

}
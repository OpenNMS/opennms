package org.opennms.core.criteria;

public class Alias {
	public static interface AliasVisitor {
		public void visitAlias(final String alias);
		public void visitAssociationPath(final String associationPath);
		public void visitType(final JoinType type);
	}

	public enum JoinType { LEFT_JOIN, INNER_JOIN, FULL_JOIN }

	private final String m_associationPath;
	private final String m_alias;
	private final JoinType m_type;

	public Alias(final String associationPath, final String alias, final JoinType type) {
		m_alias = alias.intern();
		m_associationPath = associationPath.intern();
		m_type = type;
	}
	
	public String getAlias() {
		return m_alias;
	}
	
	public String getAssociationPath() {
		return m_associationPath;
	}

	public JoinType getType() {
		return m_type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_alias == null) ? 0 : m_alias.hashCode());
		result = prime * result + ((m_associationPath == null) ? 0 : m_associationPath.hashCode());
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Alias)) return false;
		final Alias other = (Alias) obj;
		if (m_alias == null) {
			if (other.m_alias != null) return false;
		} else if (!m_alias.equals(other.m_alias)) {
			return false;
		}
		if (m_associationPath == null) {
			if (other.m_associationPath != null) return false;
		} else if (!m_associationPath.equals(other.m_associationPath)) {
			return false;
		}
		if (m_type != other.m_type) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Alias [associationPath=" + m_associationPath + ", alias=" + m_alias + ", type=" + m_type + "]";
	}

}


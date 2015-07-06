package org.opennms.core.xml;

import org.opennms.core.xml.JaxbUtils.FileType;

public final class ClassMarshallerRef {
    private final Class<?> m_class;
    private final FileType m_type;
    public ClassMarshallerRef(final Class<?> clazz, final FileType type) {
        m_class = clazz;
        m_type = type;
    }
    public Class<?> getClassObj() {
        return m_class;
    }
    public FileType getMarshallerType() {
        return m_type;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_class == null) ? 0 : m_class.getCanonicalName().hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ClassMarshallerRef)) {
            return false;
        }
        final ClassMarshallerRef other = (ClassMarshallerRef) obj;
        if (m_class == null) {
            if (other.m_class != null) {
                return false;
            }
        } else if (!m_class.getCanonicalName().equals(other.m_class.getCanonicalName())) {
            return false;
        }
        if (m_type != other.m_type) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return m_class.getName() + "(" + m_type + ")";
    }
}
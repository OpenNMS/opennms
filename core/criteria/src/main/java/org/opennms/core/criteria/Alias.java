/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.criteria;

import org.opennms.core.criteria.restrictions.Restriction;

public class Alias {
	
	
    public static interface AliasVisitor {
        public void visitAlias(final String alias);

        public void visitAssociationPath(final String associationPath);

        public void visitType(final JoinType type);
    }

    public enum JoinType {
        LEFT_JOIN, INNER_JOIN, FULL_JOIN
    }

    private final String m_associationPath;

    private final String m_alias;

    private final JoinType m_type;

    private final Restriction m_joinCondition;

    public Alias(final String associationPath, final String alias, final JoinType type, final Restriction joinCondition) {
        m_alias = alias.intern();
        m_associationPath = associationPath.intern();
        m_type = type;
        m_joinCondition = joinCondition;
    }

    public Alias(final String associationPath, final String alias, final JoinType type) {
        this(associationPath, alias, type, null);
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

    public boolean hasJoinCondition() {
        return m_joinCondition != null;
    }

    public Restriction getJoinCondition() {
        return m_joinCondition;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_alias == null) ? 0 : m_alias.hashCode());
        result = prime * result + ((m_associationPath == null) ? 0 : m_associationPath.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        result = prime * result + ((m_joinCondition == null) ? 0 : m_joinCondition.hashCode());
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
        if (m_joinCondition == null && other.m_joinCondition != null) return false;
        if (m_joinCondition != null && other.m_joinCondition == null) return false;
        if (!m_joinCondition.equals(other.m_joinCondition)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Alias [associationPath=" + m_associationPath + ", alias=" + m_alias + ", type=" + m_type + ", joinCondition=" + m_joinCondition + "]";
    }

}

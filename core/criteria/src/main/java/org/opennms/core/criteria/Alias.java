/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.criteria;

import java.util.Objects;

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

    private Restriction m_joinCondition;

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

    public void setJoinCondition(Restriction joinCondition) {
        m_joinCondition = joinCondition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_alias, m_associationPath, m_type, m_joinCondition);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Alias)) return false;
        final Alias that = (Alias) obj;
        return Objects.equals(this.m_alias, that.m_alias)
                && Objects.equals(this.m_associationPath, that.m_associationPath)
                && Objects.equals(this.m_type, that.m_type)
                && Objects.equals(this.m_joinCondition, that.m_joinCondition);
    }

    @Override
    public String toString() {
        return "Alias [associationPath=" + m_associationPath + ", alias=" + m_alias + ", type=" + m_type + ", joinCondition=" + m_joinCondition + "]";
    }

}

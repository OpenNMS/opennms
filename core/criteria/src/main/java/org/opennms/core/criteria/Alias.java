/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

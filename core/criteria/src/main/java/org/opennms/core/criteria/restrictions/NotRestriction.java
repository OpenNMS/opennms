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
package org.opennms.core.criteria.restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotRestriction extends BaseRestriction {
    private static final Logger LOG = LoggerFactory.getLogger(NotRestriction.class);
    private final Restriction m_restriction;

    public NotRestriction(final Restriction restriction) {
        super(RestrictionType.NOT);
        m_restriction = restriction;
    }

    public Restriction getRestriction() {
        return m_restriction;
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitNot(this);
        try {
            getRestriction().visit(visitor);
        } catch (final Exception e) {
            LOG.trace("Exception during restriction evaluation: {}", e.getMessage(), e);
        }
        visitor.visitNotComplete(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_restriction == null) ? 0 : m_restriction.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof NotRestriction)) return false;
        final NotRestriction other = (NotRestriction) obj;
        if (m_restriction == null) {
            if (other.m_restriction != null) return false;
        } else if (!m_restriction.equals(other.m_restriction)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NotRestriction [restriction=" + m_restriction + "]";
    }
}

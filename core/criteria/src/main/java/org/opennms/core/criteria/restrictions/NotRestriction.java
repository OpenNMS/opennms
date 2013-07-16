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

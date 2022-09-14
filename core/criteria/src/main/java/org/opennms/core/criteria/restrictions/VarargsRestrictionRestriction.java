/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria.restrictions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class VarargsRestrictionRestriction extends BaseRestriction {

    private List<Restriction> m_restrictions = new ArrayList<>();

    protected VarargsRestrictionRestriction(final RestrictionType type, final Restriction... restrictions) {
        super(type);
        Collections.addAll(m_restrictions, restrictions);
    }

    public Collection<Restriction> getRestrictions() {
        return m_restrictions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_restrictions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof VarargsRestrictionRestriction)) return false;
        final VarargsRestrictionRestriction that = (VarargsRestrictionRestriction) obj;
        return Objects.deepEquals(this.m_restrictions, that.m_restrictions);
    }

    @Override
    public String toString() {
        return "VarargsRestrictionRestriction [type=" + getType() + ", restrictions=" + m_restrictions + "]";
    }

}

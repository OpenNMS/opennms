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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class VarargsRestrictionRestriction extends BaseRestriction {

    private List<Restriction> m_restrictions = new ArrayList<>();

    public VarargsRestrictionRestriction(final RestrictionType type, final Restriction... restrictions) {
        super(type);
        for (final Restriction r : restrictions) {
            m_restrictions.add(r);
        }
    }

    public Collection<Restriction> getRestrictions() {
        return m_restrictions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + m_restrictions.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof VarargsRestrictionRestriction)) return false;
        final VarargsRestrictionRestriction other = (VarargsRestrictionRestriction) obj;
        if (!m_restrictions.equals(other.m_restrictions)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "VarargsRestrictionRestriction [type=" + getType() + ", restrictions=" + m_restrictions + "]";
    }

}

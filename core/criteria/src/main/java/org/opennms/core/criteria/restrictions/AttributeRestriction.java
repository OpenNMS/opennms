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

public abstract class AttributeRestriction extends BaseRestriction {
    private final String m_attribute;

    public AttributeRestriction(final RestrictionType type, final String attribute) {
        super(type);
        m_attribute = attribute.intern();
    }

    public String getAttribute() {
        return m_attribute;
    }

    protected static String lower(final String string) {
        return string == null ? null : string.toLowerCase();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof AttributeRestriction)) return false;
        final AttributeRestriction other = (AttributeRestriction) obj;
        if (m_attribute == null) {
            if (other.m_attribute != null) return false;
        } else if (!m_attribute.equals(other.m_attribute)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AttributeRestriction [type=" + getType() + ", attribute=" + m_attribute + "]";
    }
}

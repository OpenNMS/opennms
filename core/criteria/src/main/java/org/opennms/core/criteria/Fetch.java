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

public class Fetch {
    public enum FetchType {
        DEFAULT, LAZY, EAGER
    }

    private final String m_attribute;

    private final FetchType m_fetchType;

    public Fetch(final String attribute, final FetchType fetchType) {
        m_attribute = attribute;
        m_fetchType = fetchType;
    }

    public Fetch(final String attribute) {
        m_attribute = attribute;
        m_fetchType = FetchType.DEFAULT;
    }

    public String getAttribute() {
        return m_attribute;
    }

    public FetchType getFetchType() {
        return m_fetchType;
    }

    /*
     * we don't include m_fetchType since a single fetch attribute should only
     * be used once
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
        // result = prime * result + ((m_fetchType == null) ? 0 :
        // m_fetchType.hashCode());
        return result;
    }

    /*
     * we don't include m_fetchType since a single fetch attribute should only
     * be used once
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Fetch)) return false;
        final Fetch other = (Fetch) obj;
        if (m_attribute == null) {
            if (other.m_attribute != null) return false;
        } else if (!m_attribute.equals(other.m_attribute)) {
            return false;
        }
        // if (m_fetchType != other.m_fetchType) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Fetch [attribute=" + m_attribute + ", fetchType=" + m_fetchType + "]";
    }

}
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
package org.opennms.features.scv.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Credentials implements Serializable {
    static final long serialVersionUID = -1241293670886186178L;

    private final String m_username;
    private final String m_password;
    private final Map<String, String> m_attributes;

    public Credentials(String username, String password) {
        this(username, password, Collections.emptyMap());
    }

    public Credentials(String username, String password, Map<String, String> attributes) {
        m_username = username;
        m_password = password;
        if (attributes == null) {
            m_attributes = Collections.unmodifiableMap(Collections.emptyMap());
        } else {
            m_attributes = Collections.unmodifiableMap(Objects.requireNonNull(attributes));
        }
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    public String getAttribute(String key) {
        return m_attributes.get(key);
    }

    public Set<String> getAttributeKeys() {
        return m_attributes.keySet();
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(m_attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_attributes, m_password, m_username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Credentials other = (Credentials) obj;
        return Objects.equals(this.m_attributes, other.m_attributes) &&
                Objects.equals(this.m_password, other.m_password) &&
                Objects.equals(this.m_username, other.m_username);
    }

    @Override
    public String toString() {
        return String.format("Credentials[username=%s,password=XXXXXX]", m_username);
    }
}

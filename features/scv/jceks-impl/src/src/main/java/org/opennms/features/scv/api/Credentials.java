/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.scv.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public class Credentials implements Serializable {
    static final long serialVersionUID = -1241293670886186178L;

    private final String m_username;
    private final String m_password;
    private final ImmutableMap<String, String> m_attributes;

    public Credentials(String username, String password) {
        this(username, password, Collections.emptyMap());
    }

    public Credentials(String username, String password, Map<String, String> attributes) {
        m_username = username;
        m_password = password;
        if (attributes == null) {
            m_attributes = ImmutableMap.copyOf(Collections.emptyMap());
        } else {
            m_attributes = ImmutableMap.copyOf(Objects.requireNonNull(attributes));
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

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.discovery.messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

import com.google.common.base.Preconditions;

public class DiscoveryResults implements Serializable {
    private static final long serialVersionUID = -3416001085353754747L;

    private final Map<InetAddress, Long> m_responses;
    private final String m_foreignSource;
    private final String m_location;

    public DiscoveryResults(Map<InetAddress, Long> responses, String foreignSource, String location) {
        m_responses = Preconditions.checkNotNull(responses, "ranges argument");
        m_foreignSource = Preconditions.checkNotNull(foreignSource, "foreignSource argument");
        m_location = Preconditions.checkNotNull(location, "location argument");
    }

    public Map<InetAddress, Long> getResponses() {
        return m_responses;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public String getLocation() {
        return m_location;
    }

    @Override  
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final DiscoveryResults other = (DiscoveryResults) obj;

       return com.google.common.base.Objects.equal(this.m_responses, other.m_responses)  &&
               com.google.common.base.Objects.equal(this.m_foreignSource, other.m_foreignSource) &&
               com.google.common.base.Objects.equal(this.m_location, other.m_location);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(m_responses,
               m_foreignSource,
               m_location);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("responses", m_responses)
                 .add("foreignSource", m_foreignSource)
                 .add("location", m_location)
                 .toString();
    }
}


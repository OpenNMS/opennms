/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link.config.linkadapter;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="link-adapter-configuration")
public class LinkAdapterConfiguration {
    Set<LinkPattern> m_patterns = new HashSet<LinkPattern>();

    /**
     * <p>addPattern</p>
     *
     * @param linkPattern a {@link org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern} object.
     */
    public void addPattern(final LinkPattern linkPattern) {
        m_patterns.add(linkPattern);
    }

    /**
     * <p>getPatterns</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlElement(name="for")
    public Set<LinkPattern> getPatterns() {
        return m_patterns;
    }
    
    /**
     * <p>setPatterns</p>
     *
     * @param patterns a {@link java.util.Set} object.
     */
    public void setPatterns(final Set<LinkPattern> patterns) {
        synchronized(m_patterns) {
            if (patterns == m_patterns) return;
            m_patterns.clear();
            m_patterns.addAll(patterns);
        }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("patterns", m_patterns)
            .toString();
    }
}

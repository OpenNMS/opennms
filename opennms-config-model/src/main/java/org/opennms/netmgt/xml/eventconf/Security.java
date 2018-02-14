/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Security settings for this configuration
 */
@XmlRootElement(name="security")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Security implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Event element whose value cannot be overridden by a
     *  value in an incoming event
     */
    @XmlElement(name="doNotOverride", required=true)
    private List<String> m_doNotOverride = new ArrayList<>();

    public List<String> getDoNotOverrides() {
        return m_doNotOverride;
    }

    public void setDoNotOverride(final List<String> doNotOverride) {
        ConfigUtils.assertMinimumSize(doNotOverride, 1, "doNotOverride");
        if (m_doNotOverride == doNotOverride) return;
        m_doNotOverride.clear();
        if (doNotOverride != null) m_doNotOverride.addAll(doNotOverride);
    }

    public void addDoNotOverride(final String doNotOverride) {
        m_doNotOverride.add(doNotOverride);
    }

    public boolean removeDoNotOverride(final String doNotOverride) {
        return m_doNotOverride.remove(doNotOverride);
    }

    public boolean isSecureTag(final String tag) {
        return m_doNotOverride == null ? false : m_doNotOverride.contains(tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_doNotOverride);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Security) {
            final Security that = (Security) obj;
            return Objects.equals(this.m_doNotOverride, that.m_doNotOverride);
        }
        return false;
    }

}

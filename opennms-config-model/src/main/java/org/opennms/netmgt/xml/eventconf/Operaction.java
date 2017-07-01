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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.NullStringAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The operator action to be taken when this event occurs
 * with state controlling if action takes place. The menutext gets
 * displayed in the UI.
 */
@XmlRootElement(name="operaction")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_state", "m_menutext", "m_content"})
public class Operaction implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlValue
    @XmlJavaTypeAdapter(NullStringAdapter.class)
    private String m_content;

    @XmlAttribute(name="state")
    private StateType m_state;

    // @NotNull
    @XmlAttribute(name="menutext", required=true)
    private String m_menutext;

    public String getContent() {
        return m_content;
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeString(content);
        if (m_content != null) m_content = m_content.intern();
    }

    public StateType getState() {
        return m_state == null? StateType.ON : m_state; // Default state is "on" according to the XSD
    }

    public void setState(final StateType state) {
        m_state = state;
    }

    public String getMenutext() {
        return m_menutext;
    }

    public void setMenutext(final String menutext) {
        m_menutext = ConfigUtils.assertNotEmpty(menutext, "menutext").intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_content == null) ? 0 : m_content.hashCode());
        result = prime * result + ((m_menutext == null) ? 0 : m_menutext.hashCode());
        result = prime * result + ((m_state == null) ? 0 : m_state.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Operaction)) return false;
        final Operaction other = (Operaction) obj;
        if (m_content == null) {
            if (other.m_content != null) return false;
        } else if (!m_content.equals(other.m_content)) {
            return false;
        }
        if (m_menutext == null) {
            if (other.m_menutext != null) return false;
        } else if (!m_menutext.equals(other.m_menutext)) {
            return false;
        }
        if (m_state == null) {
            if (other.m_state != null) return false;
        } else if (!m_state.equals(other.m_state)) {
            return false;
        }
        return true;
    }

}

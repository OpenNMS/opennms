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
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.NullStringAdapter;
import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * The autoacknowledge information for the user with state
 * controlling if event is marked acknowledged when inserted into
 * the database
 */
@XmlRootElement(name="autoacknowledge")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Autoacknowledge implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlValue
    @XmlJavaTypeAdapter(NullStringAdapter.class)
    private String m_content;

    @XmlAttribute(name="state", required=false)
    private StateType m_state;

    public String getContent() {
        return m_content;
    }

    public StateType getState() {
        return m_state == null? StateType.ON : m_state; // XSD default is on
    }

    public void setContent(final String content) {
        m_content = ConfigUtils.normalizeAndInternString(content);
    }

    public void setState(final StateType state) {
        m_state = state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_content, m_state);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Autoacknowledge) {
            final Autoacknowledge that = (Autoacknowledge) obj;
            return Objects.equals(this.m_content, that.m_content) &&
                    Objects.equals(this.m_state, that.m_state);
        }
        return false;
    }


}

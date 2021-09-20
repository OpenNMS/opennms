/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notificationCommands;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notificationCommands.xsd")
public class Argument implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "streamed", required = true)
    private Boolean m_streamed;

    @XmlElement(name = "substitution")
    private String m_substitution;

    @XmlElement(name = "switch")
    private String m_switch;

    public Argument() {
    }

    public Boolean getStreamed() {
        return m_streamed;
    }

    public void setStreamed(final Boolean streamed) {
        m_streamed = ConfigUtils.assertNotNull(streamed, "streamed");
    }

    public Optional<String> getSubstitution() {
        return Optional.ofNullable(m_substitution);
    }

    public void setSubstitution(final String substitution) {
        m_substitution = ConfigUtils.normalizeString(substitution);
    }

    public Optional<String> getSwitch() {
        return Optional.ofNullable(this.m_switch);
    }

    public void setSwitch(final String s) {
        m_switch = ConfigUtils.normalizeString(s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_streamed, m_substitution,  m_switch);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Argument) {
            final Argument that = (Argument)obj;
            return Objects.equals(this.m_streamed, that.m_streamed)
                    && Objects.equals(this.m_substitution, that.m_substitution)
                    && Objects.equals(this.m_switch, that.m_switch);
        }
        return false;
    }

}

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

package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "argument")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class Argument implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * internal content storage
     */
    @XmlValue
    private String m_value;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    public Argument() {
    }

    public Argument(final String type, final String value) {
        setType(type);
        setValue(value);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(m_value);
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.normalizeString(value);
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public int hashCode() {
        return Objects.hash(m_value, m_type);
    }

    @Override()
    public boolean equals(final java.lang.Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Argument) {
            final Argument that = (Argument) obj;
            return Objects.equals(this.m_value, that.m_value) &&
                    Objects.equals(this.m_type, that.m_type);
        }
        return false;
    }
}

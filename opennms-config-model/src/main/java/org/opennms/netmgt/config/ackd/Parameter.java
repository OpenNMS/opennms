/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ackd;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Parameters to be used for collecting this service. Parameters are specific
 * to the service monitor.
 */
@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ackd-configuration.xsd")
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "key")
    private String m_key;

    @XmlAttribute(name = "value")
    private String m_value;

    public Parameter() {
    }

    public Parameter(final String key, final String value) {
        setKey(key);
        setValue(value);
    }

    public String getKey() {
        return this.m_key;
    }

    public void setKey(final String key) {
        m_key = key;
    }

    public String getValue() {
        return this.m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    public int hashCode() {
        return Objects.hash(m_key, m_value);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Parameter) {
            final Parameter that = (Parameter) obj;
            return Objects.equals(this.m_key, that.m_key) &&
                    Objects.equals(this.m_value, that.m_value);
        }
        return false;
    }
}

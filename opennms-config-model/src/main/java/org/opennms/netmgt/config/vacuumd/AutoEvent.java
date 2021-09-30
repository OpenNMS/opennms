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

package org.opennms.netmgt.config.vacuumd;

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

/**
 * (THIS IS BEING DEPRECATED) actions modify the database based on results of
 * a trigger
 */
@XmlRootElement(name = "auto-event")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class AutoEvent implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "fields")
    private String m_fields;

    /**
     * Must be a UEI defined in event-conf.xml
     */
    @XmlElement(name = "uei", required = true)
    private Uei m_uei;

    public AutoEvent() {
    }

    public AutoEvent(final String name, final String fields, final Uei uei) {
        setName(name);
        setFields(fields);
        setUei(uei);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getFields() {
        return Optional.ofNullable(m_fields);
    }

    public void setFields(final String fields) {
        m_fields = ConfigUtils.normalizeString(fields);
    }

    public Uei getUei() {
        return m_uei;
    }

    public void setUei(final Uei uei) {
        m_uei = ConfigUtils.assertNotNull(uei, "uei");
    }

    public int hashCode() {
        return Objects.hash(m_name, m_fields, m_uei);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AutoEvent) {
            final AutoEvent that = (AutoEvent) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_fields, that.m_fields) &&
                    Objects.equals(this.m_uei, that.m_uei);
        }
        return false;
    }
}

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

package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Service to be launched by the manager.
 */
@XmlRootElement(name = "service")
@ValidateUsing("service-configuration.xsd")
public class Service implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "enabled")
    private Boolean m_enabled;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "class-name", required = true)
    private String m_className;

    @XmlElement(name = "attribute")
    private List<Attribute> m_attributes = new ArrayList<>();

    @XmlElement(name = "invoke")
    private List<Invoke> m_invokes = new ArrayList<>();

    public Service() {
    }

    public Service(final String name, final String className,
        final List<Attribute> attributes, final List<Invoke> invokes) {
        setName(name);
        setClassName(className);
        setAttributes(attributes);
        setInvokes(invokes);
    }

    @XmlTransient
    public Boolean isEnabled() {
        return m_enabled == null? Boolean.TRUE : m_enabled;
    }

    public void setEnabled(final Boolean enabled) {
        m_enabled = enabled;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    @XmlTransient
    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = ConfigUtils.assertNotEmpty(className, "class-name");
    }

    @XmlTransient
    public List<Attribute> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(final List<Attribute> attributes) {
        if (attributes == m_attributes) return;
        m_attributes.clear();
        if (attributes != null) m_attributes.addAll(attributes);
    }

    public void addAttribute(final Attribute attribute) {
        m_attributes.add(attribute);
    }

    public boolean removeAttribute(final Attribute attribute) {
        return m_attributes.remove(attribute);
    }

    @XmlTransient
    public List<Invoke> getInvokes() {
        return m_invokes;
    }

    public void setInvokes(final List<Invoke> invokes) {
        if (invokes == m_invokes) return;
        m_invokes.clear();
        if (invokes != null) m_invokes.addAll(invokes);
    }

    public void addInvoke(final Invoke invoke) {
        m_invokes.add(invoke);
    }

    public boolean removeInvoke(final Invoke invoke) {
        return m_invokes.remove(invoke);
    }

    public int hashCode() {
        return Objects.hash(m_enabled, m_name, m_className, m_attributes, m_invokes);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Service) {
            final Service that = (Service) obj;
            return Objects.equals(this.m_enabled, that.m_enabled) &&
                    Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_className, that.m_className) &&
                    Objects.equals(this.m_attributes, that.m_attributes) &&
                    Objects.equals(this.m_invokes, that.m_invokes);
        }
        return false;
    }

}

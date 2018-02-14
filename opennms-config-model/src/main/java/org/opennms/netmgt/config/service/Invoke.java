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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "invoke")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class Invoke implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "method")
    private String m_method;

    @XmlAttribute(name = "pass")
    private Integer m_pass = 0;

    @XmlAttribute(name = "at")
    private InvokeAtType m_at;

    @XmlElement(name = "argument")
    private List<Argument> m_arguments = new ArrayList<>();

    public Invoke() {
    }

    public Invoke(final InvokeAtType type, final Integer pass,
        final String method, final List<Argument> arguments) {
        setAt(type);
        setPass(pass);
        setMethod(method);
        setArguments(arguments);
    }

    public String getMethod() {
        return m_method;
    }

    public void setMethod(final String method) {
        m_method = ConfigUtils.assertNotEmpty(method, "method");
    }

    public int getPass() {
        return m_pass == null? 0 : m_pass;
    }

    public void setPass(final int pass) {
        m_pass = pass;
    }

    public InvokeAtType getAt() {
        return m_at;
    }

    public void setAt(final InvokeAtType at) {
        m_at = ConfigUtils.assertNotNull(at, "at");
    }

    public List<Argument> getArguments() {
        return m_arguments;
    }

    public void setArguments(final List<Argument> arguments) {
        if (arguments == m_arguments) return;
        m_arguments.clear();
        if (arguments != null) m_arguments.addAll(arguments);
    }

    public void addArgument(final Argument argument) {
        m_arguments.add(argument);
    }

    public boolean removeArgument(final Argument argument) {
        return m_arguments.remove(argument);
    }

    public int hashCode() {
        return Objects.hash(m_method, m_pass, m_at, m_arguments);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Invoke) {
            final Invoke that = (Invoke) obj;
            return Objects.equals(this.m_method, that.m_method) &&
                    Objects.equals(this.m_pass, that.m_pass) &&
                    Objects.equals(this.m_at, that.m_at) &&
                    Objects.equals(this.m_arguments, that.m_arguments);
        }
        return false;
    }
}

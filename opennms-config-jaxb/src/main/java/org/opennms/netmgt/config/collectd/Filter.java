/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * A rule which addresses belonging to this package must
 *  pass. This package is applied only to addresses that pass this
 *  filter.
 */

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.NONE)
public class Filter implements Serializable {
    private static final long serialVersionUID = -5866771380302409645L;

    @XmlAttribute(name="name")
    private String m_name;

    @XmlValue()
    private String m_content;

    public Filter() {
        super();
    }

    public Filter(final String filter) {
        this();
        m_content = filter;
    }

    public String getName() {
        return m_content;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public String getContent() {
        return m_content == null? "" : m_content;
    }

    public void setContent(final String content) {
        m_content = content;
    }

    @Override
    public int hashCode() {
        final int prime = 683;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_content == null) ? 0 : m_content.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Filter)) {
            return false;
        }
        final Filter other = (Filter) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_content == null) {
            if (other.m_content != null) {
                return false;
            }
        } else if (!m_content.equals(other.m_content)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filter [name=" + m_name + ", content=" + m_content + "]";
    }

}

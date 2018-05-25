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

package org.opennms.netmgt.config.poller;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Parameters to be used for polling this service. E.g.: for polling HTTP, the
 * URL to hit is configurable via a parameter. Parameters are specific to the
 * service monitor.
 */

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class Parameter implements Serializable {
    private static final long serialVersionUID = 8611979898730492432L;

    /**
     * Field m_key.
     */
    @XmlAttribute(name="key")
    private String m_key;

    /**
     * Field m_value.
     */
    @XmlAttribute(name="value")
    private String m_value;

    /**
     * Field m_contents.
     */
    @XmlAnyElement(lax=false)
    @XmlJavaTypeAdapter(PollerClassObjectAdapter.class)
    private Object m_contents;


    public Parameter() {
        super();
    }

    public Parameter(final String key, final String value) {
        this();
        m_key = key;
        m_value = value;
    }

    public String getKey() {
        return m_key;
    }

    public void setKey(final String key) {
        m_key = key;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    public Object getAnyObject() {
        return m_contents;
    }

    public void setAnyObject(final Object anyObject) {
        m_contents = anyObject;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_contents == null) ? 0 : m_contents.hashCode());
        result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
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
        if (!(obj instanceof Parameter)) {
            return false;
        }
        final Parameter other = (Parameter) obj;
        if (m_contents == null) {
            if (other.m_contents != null) {
                return false;
            }
        } else if (!m_contents.equals(other.m_contents)) {
            return false;
        }
        if (m_key == null) {
            if (other.m_key != null) {
                return false;
            }
        } else if (!m_key.equals(other.m_key)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) {
                return false;
            }
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Parameter [key=" + m_key + ", value=" + m_value + ", contents=" + m_contents + "]";
    }
}

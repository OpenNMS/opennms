/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.Reader;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;

/**
 * Optional event parameter
 * 
 * @author <a href="mailto:agaue@opennms.org>Alejandro Galue</a>
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Parameter implements Serializable {

    private static final long serialVersionUID = -2065585817005972567L;

    @XmlAttribute(name="name", required=true)
    private String m_name;

    @XmlAttribute(name="value", required=true)
    private String m_value;

    @XmlAttribute(name="expand", required=false)
    private Boolean m_expand;

    public boolean hasName() {
        return m_name != null ? true : false;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    public boolean hasValue() {
        return m_value != null ? true : false; 
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    public boolean hasExpand() {
        return m_expand != null ? true : false; 
    }

    public Boolean isExpand() {
        return m_expand == null ? Boolean.FALSE : m_expand;
    }

    public void setExpand(final Boolean expand) {
        m_expand = expand;
    }

    public static Parameter unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Parameter) Unmarshaller.unmarshal(Parameter.class, reader);
    }

    public void validate() throws ValidationException {
        Validator validator = new Validator();
        validator.validate(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Parameter)) return false;
        final Parameter other = (Parameter) obj;
        if (m_name == null) {
            if (other.m_name != null) return false;
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) return false;
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

}
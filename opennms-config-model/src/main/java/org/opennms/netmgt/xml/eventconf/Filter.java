/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

@XmlRootElement(name="filter")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Filter implements Serializable {
	private static final long serialVersionUID = -5048479164713766981L;

	// @NotNull
    @XmlAttribute(name="eventparm", required=true)
	private String m_eventparm;

    // @NotNull
    @XmlAttribute(name="pattern", required=true)
    private String m_pattern;
    
    // @NotNull
    @XmlAttribute(name="replacement", required=true)
    private String m_replacement;

    public String getEventparm() {
        return m_eventparm;
    }

    public String getPattern() {
        return m_pattern;
    }

    public String getReplacement() {
        return m_replacement;
    }

    /**
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void setEventparm(final String eventparm) {
        m_eventparm = eventparm.intern();
    }

    public void setPattern(final String pattern) {
        m_pattern = pattern.intern();
    }

    public void setReplacement(final String replacement) {
        m_replacement = replacement.intern();
    }

    public static Filter unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Filter) Unmarshaller.unmarshal(Filter.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_eventparm == null) ? 0 : m_eventparm.hashCode());
		result = prime * result + ((m_pattern == null) ? 0 : m_pattern.hashCode());
		result = prime * result + ((m_replacement == null) ? 0 : m_replacement.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Filter)) return false;
		final Filter other = (Filter) obj;
		if (m_eventparm == null) {
			if (other.m_eventparm != null) return false;
		} else if (!m_eventparm.equals(other.m_eventparm)) {
			return false;
		}
		if (m_pattern == null) {
			if (other.m_pattern != null) return false;
		} else if (!m_pattern.equals(other.m_pattern)) {
			return false;
		}
		if (m_replacement == null) {
			if (other.m_replacement != null) return false;
		} else if (!m_replacement.equals(other.m_replacement)) {
			return false;
		}
		return true;
	}

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import javax.xml.bind.annotation.XmlValue;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * The forwarding information for this event - state determines
 * if event is forwarded, mechanism determines how event is forwarded
 */
@XmlRootElement(name="forward")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Forward implements Serializable {
	private static final long serialVersionUID = 3228625263305682743L;

	@XmlValue
    private String m_content = "";

	@XmlAttribute(name="state", required=false)
    private String m_state;

	// @Pattern(regexp="(snmpudp|snmptcp|xmltcp|xmludp)")
	@XmlAttribute(name="mechanism", required=false)
    private String m_mechanism;

    public String getContent() {
        return m_content;
    }

    public String getMechanism() {
        return m_mechanism == null? "snmpudp" : m_mechanism; // Defaults to snmpudp in the XSD
    }

    public String getState() {
        return m_state == null? "off" : m_state; // Defaults to off in the XSD
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

    public void setContent(final String content) {
        m_content = content.intern();
    }

    public void setMechanism(final String mechanism) {
        m_mechanism = mechanism.intern();
    }

    public void setState(final String state) {
        m_state = state.intern();
    }

    public static Forward unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Forward) Unmarshaller.unmarshal(Forward.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_content == null) ? 0 : m_content.hashCode());
		result = prime * result + ((m_mechanism == null) ? 0 : m_mechanism.hashCode());
		result = prime * result + ((m_state == null) ? 0 : m_state.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Forward)) return false;
		final Forward other = (Forward) obj;
		if (m_content == null) {
			if (other.m_content != null) return false;
		} else if (!m_content.equals(other.m_content)) {
			return false;
		}
		if (m_mechanism == null) {
			if (other.m_mechanism != null) return false;
		} else if (!m_mechanism.equals(other.m_mechanism)) {
			return false;
		}
		if (m_state == null) {
			if (other.m_state != null) return false;
		} else if (!m_state.equals(other.m_state)) {
			return false;
		}
		return true;
	}

}

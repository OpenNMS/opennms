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
 * The event logmsg with the destination attribute defining
 * if event is for display only, logonly, log and display or
 * neither. A destination attribute of 'donotpersist' indicates
 * that Eventd persist the event to the database. A value of
 * 'discardtraps' instructs the SNMP trap daemon to not create
 * events for incoming traps that match this event. The optional
 * notify attributed can be used to suppress notices on a
 * particular event (by default it is true - i.e. a notice
 * will be sent).
 */
@XmlRootElement(name="logmsg")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Logmsg implements Serializable {
	private static final long serialVersionUID = 385279987964113028L;

	@XmlValue
    private String m_content = "";

	@XmlAttribute(name="notify", required=false)
    private Boolean m_notify;

	// @Pattern(regexp="(logndisplay|displayonly|logonly|suppress|donotpersist|discardtraps)")
	@XmlAttribute(name="dest", required=false)
    private String m_dest;

    public void deleteNotify() {
        m_notify = null;
    }

    public String getContent() {
        return m_content;
    }

    public String getDest() {
        return m_dest == null ? "logndisplay" : m_dest; // Default is "logndisplay" according to XSD
    }

    public Boolean getNotify() {
        return m_notify == null ? Boolean.TRUE : m_notify; // Default is true according to XSD
    }

    public boolean hasNotify() {
        return m_notify != null;
    }

    public boolean isNotify() {
        return getNotify();
    }

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

    public void setDest(final String dest) {
        m_dest = dest.intern();
    }

    public void setNotify(final boolean notify) {
        m_notify = notify;
    }

    public static Logmsg unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Logmsg) Unmarshaller.unmarshal(Logmsg.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_content == null) ? 0 : m_content.hashCode());
		result = prime * result + ((m_dest == null) ? 0 : m_dest.hashCode());
		result = prime * result + ((m_notify == null) ? 0 : m_notify.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Logmsg)) return false;
		final Logmsg other = (Logmsg) obj;
		if (m_content == null) {
			if (other.m_content != null) return false;
		} else if (!m_content.equals(other.m_content)) {
			return false;
		}
		if (m_dest == null) {
			if (other.m_dest != null) return false;
		} else if (!m_dest.equals(other.m_dest)) {
			return false;
		}
		if (m_notify == null) {
			if (other.m_notify != null) return false;
		} else if (!m_notify.equals(other.m_notify)) {
			return false;
		}
		return true;
	}

}

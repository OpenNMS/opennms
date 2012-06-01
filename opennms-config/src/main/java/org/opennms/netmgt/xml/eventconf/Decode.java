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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * This element is used for converting event 
 *  varbind value in static decoded string.
 */
@XmlRootElement(name="decode")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Decode implements Serializable {
	private static final long serialVersionUID = 3617401172106159899L;

	// @NotNull
	@XmlAttribute(name="varbindvalue", required=true)
    private String m_varbindvalue;

	// @NotNull
	@XmlAttribute(name="varbinddecodedstring",required=true)
    private String m_varbinddecodedstring;

    public String getVarbinddecodedstring() {
        return m_varbinddecodedstring;
    }

    public String getVarbindvalue() {
        return m_varbindvalue;
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

    public void setVarbinddecodedstring(final String varbinddecodedstring) {
        m_varbinddecodedstring = varbinddecodedstring.intern();
    }

    public void setVarbindvalue(final String varbindvalue) {
        m_varbindvalue = varbindvalue.intern();
    }

    public static Decode unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Decode) Unmarshaller.unmarshal(Decode.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_varbinddecodedstring == null) ? 0 : m_varbinddecodedstring.hashCode());
		result = prime * result + ((m_varbindvalue == null) ? 0 : m_varbindvalue.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Decode)) return false;
		final Decode other = (Decode) obj;
		if (m_varbinddecodedstring == null) {
			if (other.m_varbinddecodedstring != null) return false;
		} else if (!m_varbinddecodedstring.equals(other.m_varbinddecodedstring)) {
			return false;
		}
		if (m_varbindvalue == null) {
			if (other.m_varbindvalue != null) return false;
		} else if (!m_varbindvalue.equals(other.m_varbindvalue)) {
			return false;
		}
		return true;
	}

}

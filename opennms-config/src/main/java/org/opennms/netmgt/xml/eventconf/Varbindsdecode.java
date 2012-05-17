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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
@XmlRootElement(name="varbindsdecode")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_parmid", "m_decodes"})
public class Varbindsdecode implements Serializable {
	private static final long serialVersionUID = -6483547334892439888L;
	private static final Decode[] EMPTY_DECODE_ARRAY = new Decode[0];

	/**
     * The identifier of the parameters to be decoded
     */
	// @NotNull
	@XmlElement(name="parmid", required=true)
    private String m_parmid;

    /**
     * The value to string decoding map
     */
	// @Size(min=1)
	// @NotNull
	@XmlElement(name="decode", required=true)
    private List<Decode> m_decodes = new ArrayList<Decode>();

    public String getParmid() {
        return m_parmid;
    }

    public void setParmid(final String parmid) {
        m_parmid = parmid == null? null : parmid.intern();
    }

    public Decode[] getDecode() {
        return m_decodes.toArray(EMPTY_DECODE_ARRAY);
    }

    public Decode getDecode(final int index) throws IndexOutOfBoundsException {
        return m_decodes.get(index);
    }

    public java.util.List<Decode> getDecodeCollection() {
        return m_decodes;
    }

    public int getDecodeCount() {
        return m_decodes.size();
    }

    public java.util.Enumeration<Decode> enumerateDecode() {
        return java.util.Collections.enumeration(m_decodes);
    }

    public java.util.Iterator<Decode> iterateDecode() {
        return m_decodes.iterator();
    }

    public void setDecode(final List<Decode> decodes) {
        m_decodes.clear();
        m_decodes.addAll(decodes);
    }

    public void setDecode(final int index, final Decode decode) throws IndexOutOfBoundsException {
        m_decodes.set(index, decode);
    }

    public void setDecode(final Decode[] decodes) {
        m_decodes.clear();
        for (final Decode decode : decodes) {
        	m_decodes.add(decode);
        }
    }

    public void setDecodeCollection(final List<Decode> decodes) {
        m_decodes = decodes;
    }

	public void addDecode(final Decode decode) throws IndexOutOfBoundsException {
        m_decodes.add(decode);
    }

    public void addDecode(final int index, final Decode decode) throws IndexOutOfBoundsException {
        m_decodes.add(index, decode);
    }

    public void removeAllDecode() {
        m_decodes.clear();
    }

    public boolean removeDecode(final Decode decode) {
        return m_decodes.remove(decode);
    }

    public Decode removeDecodeAt(final int index) {
        return m_decodes.remove(index);
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

    public static Varbindsdecode unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Varbindsdecode) Unmarshaller.unmarshal(Varbindsdecode.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_decodes == null) ? 0 : m_decodes.hashCode());
		result = prime * result + ((m_parmid == null) ? 0 : m_parmid.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Varbindsdecode)) return false;
		final Varbindsdecode other = (Varbindsdecode) obj;
		if (m_decodes == null) {
			if (other.m_decodes != null) return false;
		} else if (!m_decodes.equals(other.m_decodes)) {
			return false;
		}
		if (m_parmid == null) {
			if (other.m_parmid != null) return false;
		} else if (!m_parmid.equals(other.m_parmid)) {
			return false;
		}
		return true;
	}

}

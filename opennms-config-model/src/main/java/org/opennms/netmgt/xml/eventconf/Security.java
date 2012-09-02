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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * Security settings for this configuration
 */
@XmlRootElement(name="security")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
public class Security implements Serializable {
	private static final long serialVersionUID = -3138224695711877257L;
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
     * Event element whose value cannot be overridden by a
     *  value in an incoming event
     */
	// @NotNull
	// @Size(min=1)
	@XmlElement(name="doNotOverride", required=true)
    private List<String> m_doNotOverride = new ArrayList<String>();

    public void addDoNotOverride(final String doNotOverride) throws IndexOutOfBoundsException {
        m_doNotOverride.add(doNotOverride);
    }

    public void addDoNotOverride(final int index, final String doNotOverride) throws IndexOutOfBoundsException {
        m_doNotOverride.add(index, doNotOverride);
    }

    public Enumeration<String> enumerateDoNotOverride() {
        return Collections.enumeration(m_doNotOverride);
    }

    public String getDoNotOverride(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_doNotOverride.size()) {
            throw new IndexOutOfBoundsException("getDoNotOverride: Index value '" + index + "' not in range [0.." + (m_doNotOverride.size() - 1) + "]");
        }
        return m_doNotOverride.get(index);
    }

    public String[] getDoNotOverride() {
        return m_doNotOverride.toArray(EMPTY_STRING_ARRAY);
    }

    public List<String> getDoNotOverrideCollection() {
        return m_doNotOverride;
    }

    public int getDoNotOverrideCount() {
        return m_doNotOverride.size();
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

    public Iterator<String> iterateDoNotOverride() {
        return m_doNotOverride.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllDoNotOverride() {
        m_doNotOverride.clear();
    }

    public boolean removeDoNotOverride(final String doNotOverride) {
        return m_doNotOverride.remove(doNotOverride);
    }

    public String removeDoNotOverrideAt(final int index) {
        return m_doNotOverride.remove(index);
    }

    public void setDoNotOverride(final int index, final String doNotOverride) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_doNotOverride.size()) {
            throw new IndexOutOfBoundsException("setDoNotOverride: Index value '" + index + "' not in range [0.." + (m_doNotOverride.size() - 1) + "]");
        }
        m_doNotOverride.set(index, doNotOverride);
    }

    public void setDoNotOverride(final String[] doNotOverride) {
        m_doNotOverride.clear();
        for (final String dno : doNotOverride) {
        	m_doNotOverride.add(dno);
        }
    }

    public void setDoNotOverride(final List<String> doNotOverride) {
        m_doNotOverride.clear();
        m_doNotOverride.addAll(doNotOverride);
    }

    public void setDoNotOverrideCollection(final List<String> doNotOverride) {
        m_doNotOverride = doNotOverride;
    }

    public static Security unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Security) Unmarshaller.unmarshal(Security.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_doNotOverride == null) ? 0 : m_doNotOverride.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Security)) return false;
		final Security other = (Security) obj;
		if (m_doNotOverride == null) {
			if (other.m_doNotOverride != null) return false;
		} else if (!m_doNotOverride.equals(other.m_doNotOverride)) {
			return false;
		}
		return true;
	}

}

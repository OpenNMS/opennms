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
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * The mask element
 */
@XmlRootElement(name="maskelement")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={"m_name", "m_values"})
public class Maskelement implements Serializable {
	private static final long serialVersionUID = -3932312038903008806L;
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	// @NotNull
	@XmlElement(name="mename", required=true)
    private String m_name;

	// @NotNull
	// @Size(min=1)
	@XmlElement(name="mevalue", required=true)
    private List<String> m_values = new ArrayList<String>();

    public void addMevalue(final String value) throws IndexOutOfBoundsException {
        m_values.add(value.intern());
    }

    public void addMevalue(final int index, final String value) throws IndexOutOfBoundsException {
        m_values.add(index, value.intern());
    }

    public Enumeration<String> enumerateMevalue() {
        return Collections.enumeration(m_values);
    }

    /**
     * <p>
     * The mask element name. Must be from the following subset:
     * </p>
     * <dl>
     * <dt>uei</dt><dd>the OpenNMS Universal Event Identifier</dd>
     * <dt>source</dt><dd>source of the event; "trapd" for received SNMP traps;
     * warning: these aren't standardized</dd>
     * <dt>host</dt><dd>host related to the event; for SNMP traps this is the
     * IP source address of the host that sent the trap to OpenNMS</dd>
     * <dt>snmphost</dt><dd>SNMP host related to  the event; for SNMPv1 traps
     * this is IP address reported in the trap; for SNMPv2 traps and later this
     * is the same as "host"</dd>
     * <dt>nodeid</dt><dd>the OpenNMS node identifier for the node related
     * to this event</dd>
     * <dt>interface</dt><dd>interface related to the event; for SNMP
     * traps this is the same as "snmphost"</dd>
     * <dt>service</dt><dd>Service name</dd>
     * <dt>id</dt><dd>enterprise ID in an SNMP trap</dd>
     * <dt>specific</dt><dd>specific value in an SNMP trap</dd>
     * <dt>generic</dt><dd>generic value in an SNMP trap</dd>
     * <dt>community</dt><dd>community string in an SNMP trap</dd>
     * </dl>
     */
    public String getMename() {
        return m_name;
    }

    /**
     * The mask element value. A case-sensitive, exact match is performed.
     * If the mask value has a "%" as the last character, it will match zero
     * or more characters at the end of the string being matched.
     */
    public String getMevalue(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_values.size()) {
            throw new IndexOutOfBoundsException("getMevalue: Index value '" + index + "' not in range [0.." + (m_values.size() - 1) + "]");
        }
        return m_values.get(index);
    }

    public String[] getMevalue() {
        return m_values.toArray(EMPTY_STRING_ARRAY);
    }

    public List<String> getMevalueCollection() {
        return m_values;
    }

    public int getMevalueCount() {
        return m_values.size();
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

    public Iterator<String> iterateMevalue() {
        return m_values.iterator();
    }

    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllMevalue() {
        m_values.clear();
    }

    public boolean removeMevalue(final String value) {
        return m_values.remove(value);
    }

    public String removeMevalueAt(final int index) {
        return m_values.remove(index);
    }

    public void setMename(final String mename) {
        m_name = mename.intern();
    }

    public void setMevalue(final int index, final String value) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_values.size()) {
            throw new IndexOutOfBoundsException("setMevalue: Index value '" + index + "' not in range [0.." + (m_values.size() - 1) + "]");
        }
        m_values.set(index, value.intern());
    }

    public void setMevalue(final String[] values) {
        m_values.clear();
        for (final String value : values) {
        	m_values.add(value.intern());
        }
    }

    public void setMevalue(final List<String> values) {
        m_values.clear();
        for (final String value : values) {
            m_values.add(value.intern());
        }
    }

    public void setMevalueCollection(final List<String> values) {
        m_values.clear();
        for (final String value : values) {
            m_values.add(value.intern());
        }
    }

    public static Maskelement unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Maskelement) Unmarshaller.unmarshal(Maskelement.class, reader);
    }

    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result + ((m_values == null) ? 0 : m_values.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Maskelement)) return false;
		final Maskelement other = (Maskelement) obj;
		if (m_name == null) {
			if (other.m_name != null) return false;
		} else if (!m_name.equals(other.m_name)) {
			return false;
		}
		if (m_values == null) {
			if (other.m_values != null) return false;
		} else if (!m_values.equals(other.m_values)) {
			return false;
		}
		return true;
	}

}

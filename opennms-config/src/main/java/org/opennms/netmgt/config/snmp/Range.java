/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.snmp;

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
import org.xml.sax.ContentHandler;

/**
 * IP Address Range
 */

@XmlRootElement(name="range")
@XmlAccessorType(XmlAccessType.FIELD)
public class Range implements Serializable {
	private static final long serialVersionUID = 3386982883357355619L;
	

	/* Note: Until a way to specify the order is found...
	 * The jaxb code seems to put things in the reverse order 
	 * of the order the attributes are declared.  So PRESERVE this 
	 * ordering so we don't end up with end="" begin=""
	 */
    /**
     * Ending IP address of the range.
     */
	@XmlAttribute(name="end", required=true)
    private String _end;

    /**
     * Starting IP address of the range.
     */
    @XmlAttribute(name="begin", required=true)
    private String _begin;

    public Range() {
        super();
    }

    public Range(final String begin, String end) {
    	_begin = begin;
    	_end = end;
	}

    /**
     * Returns the value of field 'begin'. The field 'begin' has
     * the following description: Starting IP address of the range.
     * 
     * @return the value of field 'Begin'.
     */
    public String getBegin() {
        return this._begin;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the
     * following description: Ending IP address of the range.
     * 
     * @return the value of field 'End'.
     */
    public String getEnd() {
        return this._end;
    }

    /**
     * Method isValid.
     * 
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

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'begin'. The field 'begin' has the
     * following description: Starting IP address of the range.
     * 
     * @param begin the value of field 'begin'.
     */
    public void setBegin(final String begin) {
        this._begin = begin;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the
     * following description: Ending IP address of the range.
     * 
     * @param end the value of field 'end'.
     */
    public void setEnd(final String end) {
        this._end = end;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled Range
     */
    public static Range unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Range) Unmarshaller.unmarshal(Range.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "Range[begin=" + _begin + ", end=" + _end + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_begin == null) ? 0 : _begin.hashCode());
        result = prime * result + ((_end == null) ? 0 : _end.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        final Range other = (Range) obj;
        if (_begin == null) {
            if (other._begin != null) return false;
        } else if (!_begin.equals(other._begin)) {
            return false;
        }
        if (_end == null) {
            if (other._end != null) return false;
        } else if (!_end.equals(other._end)) {
            return false;
        }
        return true;
    }

}

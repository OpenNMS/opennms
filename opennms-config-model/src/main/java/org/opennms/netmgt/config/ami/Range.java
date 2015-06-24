/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.ami;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * IP Address Range
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "range")
@XmlAccessorType(XmlAccessType.FIELD)
public class Range implements Serializable {
    private static final long serialVersionUID = 8055671432443263541L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Starting IP address of the range.
     */
    @XmlAttribute(name = "begin")
    private String _begin;

    /**
     * Ending IP address of the range.
     */
    @XmlAttribute(name = "end")
    private String _end;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Range() {
        super();
    }

    public Range(final String begin, final String end) {
        super();
        setBegin(begin);
        setEnd(end);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Range) {

            Range temp = (Range) obj;
            if (this._begin != null) {
                if (temp._begin == null)
                    return false;
                else if (!(this._begin.equals(temp._begin)))
                    return false;
            } else if (temp._begin != null)
                return false;
            if (this._end != null) {
                if (temp._end == null)
                    return false;
                else if (!(this._end.equals(temp._end)))
                    return false;
            } else if (temp._end != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'begin'. The field 'begin' has the following
     * description: Starting IP address of the range.
     *
     * @return the value of field 'Begin'.
     */
    public String getBegin() {
        return this._begin;
    }

    /**
     * Returns the value of field 'end'. The field 'end' has the following
     * description: Ending IP address of the range.
     *
     * @return the value of field 'End'.
     */
    public String getEnd() {
        return this._end;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_begin != null) {
            result = 37 * result + _begin.hashCode();
        }
        if (_end != null) {
            result = 37 * result + _end.hashCode();
        }

        return result;
    }

    /**
     * Sets the value of field 'begin'. The field 'begin' has the following
     * description: Starting IP address of the range.
     *
     * @param begin
     *            the value of field 'begin'.
     */
    public void setBegin(final String begin) {
        this._begin = begin;
    }

    /**
     * Sets the value of field 'end'. The field 'end' has the following
     * description: Ending IP address of the range.
     *
     * @param end
     *            the value of field 'end'.
     */
    public void setEnd(final String end) {
        this._end = end;
    }
}

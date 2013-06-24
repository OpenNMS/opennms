/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.ackd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A very basic configuration for defining simple input to a schedule
 * (java.lang.concurrent)
 * 
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "reader-schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReaderSchedule implements Serializable {
    private static final long serialVersionUID = 3113838261541036911L;

    public static final long DEFAULT_INTERVAL = 1L;

    public static final String DEFAULT_UNIT = "m";

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _interval.
     */
    @XmlAttribute(name = "interval")
    private Long _interval;

    /**
     * Field _unit.
     */
    @XmlAttribute(name = "unit")
    private String _unit;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public ReaderSchedule() {
        super();
    }

    public ReaderSchedule(final Long interval, final String unit) {
        super();
        setInterval(interval);
        setUnit(unit);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReaderSchedule other = (ReaderSchedule) obj;
        if (_interval == null) {
            if (other._interval != null)
                return false;
        } else if (!_interval.equals(other._interval))
            return false;
        if (_unit == null) {
            if (other._unit != null)
                return false;
        } else if (!_unit.equals(other._unit))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'interval'.
     * 
     * @return the value of field 'Interval'.
     */
    public long getInterval() {
        return _interval == null ? DEFAULT_INTERVAL : _interval;
    }

    /**
     * Returns the value of field 'unit'.
     * 
     * @return the value of field 'Unit'.
     */
    public java.lang.String getUnit() {
        return _unit == null ? DEFAULT_UNIT : _unit;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_interval == null) ? 0 : _interval.hashCode());
        result = prime * result + ((_unit == null) ? 0 : _unit.hashCode());
        return result;
    }

    /**
     * Sets the value of field 'interval'.
     * 
     * @param interval
     *            the value of field 'interval'.
     */
    public void setInterval(final long interval) {
        this._interval = interval;
    }

    /**
     * Sets the value of field 'unit'.
     * 
     * @param unit
     *            the value of field 'unit'.
     */
    public void setUnit(final String unit) {
        this._unit = unit;
    }
}

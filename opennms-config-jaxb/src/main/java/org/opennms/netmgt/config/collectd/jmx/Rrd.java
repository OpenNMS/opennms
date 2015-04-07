/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.collectd.jmx;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "rrd")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class Rrd implements java.io.Serializable {

    @XmlAttribute(name = "step", required = true)
    private int _step = 0;

    @XmlElement(name = "rra", required = true)
    private java.util.List<String> _rraList = new java.util.ArrayList<String>();

    public void addRra(final String vRra)
            throws IndexOutOfBoundsException {
        this._rraList.add(vRra);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Rrd) {
            Rrd temp = (Rrd) obj;
            boolean equals = Objects.equals(this._step, temp._step)
                    && Objects.equals(this._rraList, temp._rraList);
            return equals;
        }
        return false;
    }

    public java.util.List<String> getRraCollection() {
        return this._rraList;
    }

    public int getRraCount() {
        return this._rraList.size();
    }

    public int getStep() {
        return this._step;
    }

    public boolean hasStep() {
        return this._step > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_step, _rraList);
    }

    public void setRraList(final java.util.List<String> rraList) {
        this._rraList = rraList;
    }

    public void setStep(final int step) {
        this._step = step;
    }

}

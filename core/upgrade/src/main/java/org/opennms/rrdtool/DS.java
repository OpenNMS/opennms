/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.rrdtool;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Class DS (Data Source).
 * <ul>
 * <li><b>ds.decl.normal:</b> minimal_heartbeat, min, max</li>
 * <li><b>ds.decl.cdef:</b> cdef</li>
 * <li><b>ds.decl:</b> name, type, (ds.decl.normal | ds.decl.cdef), last_ds, value, unknown_sec</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DS {

    /** The name of the database. */
    private String name;

    /** The type of the datasource. */
    private DSType type;

    /** The minimum heartbeat. */
    private Integer minHeartbeat = 0;

    /** The minimum value of the data source. Defaults to 'U' */
    private Double min = null;

    /** The maximum value of the data source. Defaults to 'U'. */
    private Double max = null;

    /** The last value. */
    private Double lastDs = Double.NaN;

    /** The value. */
    private Double value = 0.0;

    /** The unknown seconds. */
    private Integer unknownSec = 0;

    /** The CDEF (Computed Datasource). */
    private String cdef;

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * <p>The XML may contain spaces that must be removed.</p>
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @XmlElement(required=true)
    @XmlJavaTypeAdapter(DSAdapter.class)
    public DSType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(DSType type) {
        this.type = type;
    }

    /**
     * Gets the minimum heartbeat.
     *
     * @return the minimum heartbeat
     */
    @XmlElement(name="minimal_heartbeat")
    public Integer getMinHeartbeat() {
        return minHeartbeat;
    }

    /**
     * Sets the minimum heartbeat.
     *
     * @param minHeartbeat the new minimum heartbeat
     */
    public void setMinHeartbeat(Integer minHeartbeat) {
        this.minHeartbeat = minHeartbeat;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    @XmlElement
    public Double getMin() {
        return min;
    }

    /**
     * Sets the minimum value.
     *
     * @param min the new minimum value
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    @XmlElement
    public Double getMax() {
        return max;
    }

    /**
     * Sets the maximum value.
     *
     * @param max the new maximum value
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Gets the last data source value.
     *
     * @return the last data source time stamp
     */
    @XmlElement(name="last_ds")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    public Double getLastDs() {
        return lastDs;
    }

    /**
     * Sets the last data source value.
     *
     * @param lastDs the new last data source value
     */
    public void setLastDs(Double lastDs) {
        this.lastDs = lastDs;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @XmlElement
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Gets the unknown seconds.
     *
     * @return the unknown seconds
     */
    @XmlElement(name="unknown_sec")
    public Integer getUnknownSec() {
        return unknownSec;
    }

    /**
     * Sets the unknown seconds.
     *
     * @param unknownSec the new unknown seconds
     */
    public void setUnknownSec(Integer unknownSec) {
        this.unknownSec = unknownSec;
    }

    /**
     * Gets the CDEF.
     *
     * @return the CDEF
     */
    public String getCdef() {
        return cdef;
    }

    /**
     * Sets the CDEF.
     * <p>The XML may contain spaces that must be removed.</p>
     *
     * @param cdef the new CDEF
     */
    public void setCdef(String cdef) {
        this.cdef = cdef == null ? null : cdef.trim();
    }

    /**
     * Format equals.
     *
     * @param ds the DS object
     * @return true, if successful
     */
    public boolean formatEquals(DS ds) {
        if (this.name != null) {
            if (ds.name == null) return false;
            else if (!(this.name.equals(ds.name))) 
                return false;
        }
        else if (ds.name != null)
            return false;

        if (this.type != null) {
            if (ds.type == null) return false;
            else if (!(this.type.equals(ds.type))) 
                return false;
        }
        else if (ds.type != null)
            return false;

        if (this.minHeartbeat != null) {
            if (ds.minHeartbeat == null) return false;
            else if (!(this.minHeartbeat.equals(ds.minHeartbeat))) 
                return false;
        }
        else if (ds.minHeartbeat != null)
            return false;

        if (this.min != null) {
            if (ds.min == null) return false;
            else if (!(this.min.equals(ds.min))) 
                return false;
        }
        else if (ds.min != null)
            return false;

        if (this.max != null) {
            if (ds.max == null) return false;
            else if (!(this.max.equals(ds.max))) 
                return false;
        }
        else if (ds.max != null)
            return false;

        if (this.cdef != null) {
            if (ds.cdef == null) return false;
            else if (!(this.cdef.equals(ds.cdef))) 
                return false;
        }
        else if (ds.cdef != null)
            return false;

        return true;
    }
}

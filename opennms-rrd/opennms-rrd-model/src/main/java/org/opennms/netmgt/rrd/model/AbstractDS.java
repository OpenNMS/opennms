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

package org.opennms.netmgt.rrd.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Abstract DS (Data Source).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class AbstractDS {

    /** The name of the database. */
    private String name;

    /** The minimum heartbeat. */
    private Long minHeartbeat = 0L;

    /** The minimum value of the data source. Defaults to 'U' */
    private Double min = null;

    /** The maximum value of the data source. Defaults to 'U'. */
    private Double max = null;

    /** The last value. */
    private Double lastDs = Double.NaN;

    /** The value. */
    private Double value = 0.0;

    /** The unknown seconds. */
    private Long unknownSec = 0L;

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
     * Gets the minimum heartbeat.
     *
     * @return the minimum heartbeat
     */
    @XmlElement(name="minimal_heartbeat")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getMinHeartbeat() {
        return minHeartbeat;
    }

    /**
     * Sets the minimum heartbeat.
     *
     * @param minHeartbeat the new minimum heartbeat
     */
    public void setMinHeartbeat(Long minHeartbeat) {
        this.minHeartbeat = minHeartbeat;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    @XmlElement
    @XmlJavaTypeAdapter(DoubleAdapter.class)
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
    @XmlJavaTypeAdapter(DoubleAdapter.class)
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
    @XmlJavaTypeAdapter(DoubleAdapter.class)
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
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getUnknownSec() {
        return unknownSec;
    }

    /**
     * Sets the unknown seconds.
     *
     * @param unknownSec the new unknown seconds
     */
    public void setUnknownSec(Long unknownSec) {
        this.unknownSec = unknownSec;
    }

    /**
     * Checks if the data source is counter.
     *
     * @return true, if the data source is a counter
     */
    @XmlTransient
    abstract public boolean isCounter();

    /**
     * Format equals.
     *
     * @param ds the DS object
     * @return true, if successful
     */
    public boolean formatEquals(AbstractDS ds) {
        if (this.name != null) {
            if (ds.name == null) return false;
            else if (!(this.name.equals(ds.name))) 
                return false;
        }
        else if (ds.name != null)
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

        return true;
    }
}

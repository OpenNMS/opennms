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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Class Meta.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="meta")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XMeta {

    /** The start time expressed in seconds since 1970-01-01 UTC. */
    private Long start;

    /** The end time expressed in seconds since 1970-01-01 UTC. */
    private Long end;

    /** The step (interval) time expressed in seconds. */
    private Long step;

    /** The number of rows. */
    private Long rows;

    /** The number of columns. */
    private Long columns;

    /** The legend entries. */
    private List<String> legends = new ArrayList<>();

    /**
     * Gets the start.
     * <p>The start time expressed in seconds since 1970-01-01 UTC.</p>
     * 
     * @return the start
     */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getStart() {
        return start;
    }

    /**
     * Sets the start.
     *
     * @param start the new start
     */
    public void setStart(Long start) {
        this.start = start;
    }

    /**
     * Gets the end.
     * <p>The end time expressed in seconds since 1970-01-01 UTC</p>
     *
     * @return the end
     */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getEnd() {
        return end;
    }

    /**
     * Sets the end.
     *
     * @param end the new end
     */
    public void setEnd(Long end) {
        this.end = end;
    }

    /**
     * Gets the step.
     * <p>The step (interval) time expressed in seconds</p>
     * 
     * @return the step
     */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getStep() {
        return step;
    }

    /**
     * Sets the step.
     *
     * @param step the new step
     */
    public void setStep(Long step) {
        this.step = step;
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getRows() {
        return rows;
    }

    /**
     * Sets the rows.
     *
     * @param rows the new rows
     */
    public void setRows(Long rows) {
        this.rows = rows;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    @XmlElement
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getColumns() {
        return columns;
    }

    /**
     * Sets the columns.
     *
     * @param columns the new columns
     */
    public void setColumns(Long columns) {
        this.columns = columns;
    }

    /**
     * Gets the legend entries.
     *
     * @return the legend entries
     */
    @XmlElement(name="entry")
    @XmlElementWrapper(name="legend")
    public List<String> getLegends() {
        return legends;
    }

    /**
     * Sets the legend entries.
     *
     * @param entries the new legend entries
     */
    public void setLegends(List<String> entries) {
        this.legends = entries;
    }

}

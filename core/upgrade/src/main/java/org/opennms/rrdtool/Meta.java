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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class Meta.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Meta {

    /** The start. */
    @XmlElement
    private Long start;

    /** The step. */
    @XmlElement
    private Integer step;

    /** The end. */
    @XmlElement
    private Long end;

    /** The rows. */
    @XmlElement
    private Integer rows;

    /** The columns. */
    @XmlElement
    private Integer columns;

    /** The entries. */
    @XmlElement(name="entry")
    @XmlElementWrapper(name="legeng")
    private List<String> entries = new ArrayList<String>();

    /**
     * Gets the start.
     *
     * @return the start
     */
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
     * Gets the step.
     *
     * @return the step
     */
    public Integer getStep() {
        return step;
    }

    /**
     * Sets the step.
     *
     * @param step the new step
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * Gets the end.
     *
     * @return the end
     */
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
     * Gets the rows.
     *
     * @return the rows
     */
    public Integer getRows() {
        return rows;
    }

    /**
     * Sets the rows.
     *
     * @param rows the new rows
     */
    public void setRows(Integer rows) {
        this.rows = rows;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    public Integer getColumns() {
        return columns;
    }

    /**
     * Sets the columns.
     *
     * @param columns the new columns
     */
    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    /**
     * Gets the entries.
     *
     * @return the entries
     */
    public List<String> getEntries() {
        return entries;
    }

    /**
     * Sets the entries.
     *
     * @param entries the new entries
     */
    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

}

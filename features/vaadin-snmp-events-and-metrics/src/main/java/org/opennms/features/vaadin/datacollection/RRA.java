/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

/**
 * The Class RRA.
 */
public class RRA {

    /** The consolidation function. */
    private String cf;

    /** The XFF. */
    private Double xff;

    /** The steps. */
    private Integer steps;

    /** The rows. */
    private Integer rows;

    /**
     * Instantiates a new RRA.
     */
    public RRA() {}

    /**
     * Instantiates a new RRA.
     *
     * @param rra the RRA
     */
    public RRA(String rra) {
        setRra(rra);
    }

    /**
     * Gets the consolidation function.
     *
     * @return the consolidation function
     */
    public String getCf() {
        return cf;
    }

    /**
     * Sets the consolidation function.
     *
     * @param cf the new consolidation function
     */
    public void setCf(String cf) {
        this.cf = cf;
    }

    /**
     * Gets the XFF.
     *
     * @return the XFF
     */
    public Double getXff() {
        return xff;
    }

    /**
     * Sets the XFF.
     *
     * @param xff the new XFF
     */
    public void setXff(Double xff) {
        this.xff = xff;
    }

    /**
     * Gets the steps.
     *
     * @return the steps
     */
    public Integer getSteps() {
        return steps;
    }

    /**
     * Sets the steps.
     *
     * @param steps the new steps
     */
    public void setSteps(Integer steps) {
        this.steps = steps;
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
     * Gets the RRA.
     *
     * @return the RRA
     */
    public String getRra() {
        return "RRA:" + cf + ':' + xff + ':' + steps + ':' + rows;
    }

    /**
     * Sets the RRA.
     *
     * @param rra the new RRA
     */
    public void setRra(String rra) {
        String [] parts = rra.split(":");
        if (parts.length < 5)
            throw new IllegalArgumentException("Malformed RRA");
        try {
            setCf(parts[1]);
            setXff(new Double(parts[2]));
            setSteps(Integer.valueOf(parts[3]));
            setRows(Integer.valueOf(parts[4]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed RRA");
        }
    }
}

/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

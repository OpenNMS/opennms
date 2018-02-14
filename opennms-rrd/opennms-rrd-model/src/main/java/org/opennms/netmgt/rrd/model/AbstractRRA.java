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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Abstract RRA (Round Robin Archives).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class AbstractRRA {

    /** The PDP (Primary Data Points) per row. */
    private Long pdpPerRow;

    /** The rows. */
    private List<Row> rows = new ArrayList<>();

    /**
     * Creates a RRA with a single data source.
     *
     * @param dsIndex the RRA-DS index
     * @return the abstract RRA
     * @throws IllegalArgumentException the illegal argument exception
     */
    protected abstract AbstractRRA createSingleRRA(int dsIndex) throws IllegalArgumentException;

    /**
     * Gets the PDP (Primary Data Points) per row.
     *
     * @return the PDP (Primary Data Points) per row
     */
    @XmlElement(name="pdp_per_row")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getPdpPerRow() {
        return pdpPerRow;
    }

    /**
     * Sets the PDP (Primary Data Points) per row.
     *
     * @param pdpPerRow the new PDP (Primary Data Points) per row
     */
    public void setPdpPerRow(Long pdpPerRow) {
        this.pdpPerRow = pdpPerRow;
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    @XmlElement(name="row")
    @XmlElementWrapper(name="database")
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Sets the rows.
     *
     * @param rows the new rows
     */
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * Format equals.
     * 
     * @param rra the RRA object
     * @return true, if the format is equal
     */
    public boolean formatEquals(AbstractRRA rra) {
        if (this.pdpPerRow != null) {
            if (rra.pdpPerRow == null) return false;
            else if (!(this.pdpPerRow.equals(rra.pdpPerRow))) 
                return false;
        }
        else if (rra.pdpPerRow != null)
            return false;

        if (this.rows != null) {
            if (rra.rows == null) return false;
            else if (!(this.rows.size() == rra.rows.size())) 
                return false;
        }
        else if (rra.rows != null)
            return false;

        return true;
    }

    /**
     * Format mergeable.
     * <p>Two RRA can be merged if and only if the Consolidation Function and the PDP per row are the same.</p>
     * <p>The amount of rows is not strict for a merge operation.</p>
     * <ul>
     *   <li>If the sourceRra has more rows than the current RRA, the result will be truncated by the number of rows on the current RRA.</li>
     *   <li>If the sourceRra has less rows than the current RRA, the result will have a NaN window as big as the different between the number of rows.</li>
     * </ul>
     * 
     * @param sourceRra the source RRA object
     * @return true, if the format is mergeable
     */
    public abstract boolean formatMergeable(AbstractRRA sourceRra);

    /**
     * Checks for average as consolidation function.
     *
     * @return true, if the consolidation function is AVERAGE
     */
    public abstract boolean hasAverageAsCF();
}

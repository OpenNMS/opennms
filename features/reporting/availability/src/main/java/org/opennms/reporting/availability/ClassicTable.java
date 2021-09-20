/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.availability;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class ClassicTable.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "classicTable")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassicTable implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "col")
    private Col col;

    @XmlElement(name = "rows")
    private Rows rows;

    public ClassicTable() {
    }

    /**
     * Returns the value of field 'col'.
     * 
     * @return the value of field 'Col'.
     */
    public Col getCol() {
        return this.col;
    }

    /**
     * Returns the value of field 'rows'.
     * 
     * @return the value of field 'Rows'.
     */
    public Rows getRows() {
        return this.rows;
    }

    /**
     * Sets the value of field 'col'.
     * 
     * @param col the value of field 'col'.
     */
    public void setCol(final Col col) {
        this.col = col;
    }

    /**
     * Sets the value of field 'rows'.
     * 
     * @param rows the value of field 'rows'.
     */
    public void setRows(final Rows rows) {
        this.rows = rows;
    }

}

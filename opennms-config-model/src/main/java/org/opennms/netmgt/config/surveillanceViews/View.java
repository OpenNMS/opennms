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

package org.opennms.netmgt.config.surveillanceViews;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class View.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "view")
@XmlAccessorType(XmlAccessType.FIELD)
public class View implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_REFRESH_SECONDS = "300";

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "refresh-seconds")
    private String refreshSeconds;

    @XmlElement(name = "rows", required = true)
    private Rows rows;

    @XmlElement(name = "columns", required = true)
    private Columns columns;

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof View) {
            View temp = (View)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.refreshSeconds, refreshSeconds)
                && Objects.equals(temp.rows, rows)
                && Objects.equals(temp.columns, columns);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'columns'.
     * 
     * @return the value of field 'Columns'.
     */
    public Columns getColumns() {
        return this.columns;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of field 'refreshSeconds'.
     * 
     * @return the value of field 'RefreshSeconds'.
     */
    public String getRefreshSeconds() {
        return this.refreshSeconds != null ? this.refreshSeconds : DEFAULT_REFRESH_SECONDS;
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
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            refreshSeconds, 
            rows, 
            columns);
        return hash;
    }

    /**
     * Sets the value of field 'columns'.
     * 
     * @param columns the value of field 'columns'.
     */
    public void setColumns(final Columns columns) {
        this.columns = columns;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the value of field 'refreshSeconds'.
     * 
     * @param refreshSeconds the value of field 'refreshSeconds'.
     */
    public void setRefreshSeconds(final String refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
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

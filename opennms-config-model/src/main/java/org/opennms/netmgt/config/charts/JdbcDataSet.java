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

package org.opennms.netmgt.config.charts;


import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jdbc-data-set")
@XmlAccessorType(XmlAccessType.FIELD)
public class JdbcDataSet implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "db-name", required = true)
    private String dbName;

    @XmlAttribute(name = "sql", required = true)
    private String sql;

    public JdbcDataSet() {
    }

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
        
        if (obj instanceof JdbcDataSet) {
            JdbcDataSet temp = (JdbcDataSet)obj;
            boolean equals = Objects.equals(temp.dbName, dbName)
                && Objects.equals(temp.sql, sql);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'dbName'.
     * 
     * @return the value of field 'DbName'.
     */
    public String getDbName() {
        return this.dbName;
    }

    /**
     * Returns the value of field 'sql'.
     * 
     * @return the value of field 'Sql'.
     */
    public String getSql() {
        return this.sql;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            dbName, 
            sql);
        return hash;
    }

    /**
     * Sets the value of field 'dbName'.
     * 
     * @param dbName the value of field 'dbName'.
     */
    public void setDbName(final String dbName) {
        if (dbName == null) {
            throw new IllegalArgumentException("'db-name' is a required attribute!");
        }
        this.dbName = dbName;
    }

    /**
     * Sets the value of field 'sql'.
     * 
     * @param sql the value of field 'sql'.
     */
    public void setSql(final String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("'sql' is a required attribute!");
        }
        this.sql = sql;
    }

}

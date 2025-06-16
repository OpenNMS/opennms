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

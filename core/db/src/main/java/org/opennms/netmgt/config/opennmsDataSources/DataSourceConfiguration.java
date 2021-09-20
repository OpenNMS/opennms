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

package org.opennms.netmgt.config.opennmsDataSources;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top Level element for opennms-datasources.xml... a list of data sources
 *  to be used within OpenNMS.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "datasource-configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class DataSourceConfiguration implements java.io.Serializable {

    private static final long serialVersionUID = 8984943778772781422L;

    /**
     * Database connection pool configuration.
     */
    @XmlElement(name = "connection-pool")
    private ConnectionPool connectionPool;

    /**
     * Top-level element for the opennms-database.xml configuration
     *  file.
     */
    @XmlElement(name = "jdbc-data-source", required = true)
    private java.util.List<JdbcDataSource> jdbcDataSourceList;

    public DataSourceConfiguration() {
        this.jdbcDataSourceList = new java.util.ArrayList<>();
    }

    /**
     * 
     * 
     * @param vJdbcDataSource
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addJdbcDataSource(final JdbcDataSource vJdbcDataSource) throws IndexOutOfBoundsException {
        this.jdbcDataSourceList.add(vJdbcDataSource);
    }

    /**
     * 
     * 
     * @param index
     * @param vJdbcDataSource
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addJdbcDataSource(final int index, final JdbcDataSource vJdbcDataSource) throws IndexOutOfBoundsException {
        this.jdbcDataSourceList.add(index, vJdbcDataSource);
    }

    /**
     * Method enumerateJdbcDataSource.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<JdbcDataSource> enumerateJdbcDataSource() {
        return java.util.Collections.enumeration(this.jdbcDataSourceList);
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
        
        if (obj instanceof DataSourceConfiguration) {
            DataSourceConfiguration temp = (DataSourceConfiguration)obj;
            boolean equals = Objects.equals(temp.connectionPool, connectionPool)
                && Objects.equals(temp.jdbcDataSourceList, jdbcDataSourceList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'connectionPool'. The field 'connectionPool' has
     * the following description: Database connection pool configuration.
     * 
     * @return the value of field 'ConnectionPool'.
     */
    public ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Method getJdbcDataSource.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * JdbcDataSource at the given
     * index
     */
    public JdbcDataSource getJdbcDataSource(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.jdbcDataSourceList.size()) {
            throw new IndexOutOfBoundsException("getJdbcDataSource: Index value '" + index + "' not in range [0.." + (this.jdbcDataSourceList.size() - 1) + "]");
        }
        
        return (JdbcDataSource) jdbcDataSourceList.get(index);
    }

    /**
     * Method getJdbcDataSource.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public JdbcDataSource[] getJdbcDataSource() {
        JdbcDataSource[] array = new JdbcDataSource[0];
        return (JdbcDataSource[]) this.jdbcDataSourceList.toArray(array);
    }

    /**
     * Method getJdbcDataSourceCollection.Returns a reference to
     * 'jdbcDataSourceList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<JdbcDataSource> getJdbcDataSourceCollection() {
        return this.jdbcDataSourceList;
    }

    /**
     * Method getJdbcDataSourceCount.
     * 
     * @return the size of this collection
     */
    public int getJdbcDataSourceCount() {
        return this.jdbcDataSourceList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int hash = Objects.hash(
            connectionPool, 
            jdbcDataSourceList);
        return hash;
    }

    /**
     * Method iterateJdbcDataSource.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<JdbcDataSource> iterateJdbcDataSource() {
        return this.jdbcDataSourceList.iterator();
    }

    /**
     */
    public void removeAllJdbcDataSource() {
        this.jdbcDataSourceList.clear();
    }

    /**
     * Method removeJdbcDataSource.
     * 
     * @param vJdbcDataSource
     * @return true if the object was removed from the collection.
     */
    public boolean removeJdbcDataSource(final JdbcDataSource vJdbcDataSource) {
        boolean removed = jdbcDataSourceList.remove(vJdbcDataSource);
        return removed;
    }

    /**
     * Method removeJdbcDataSourceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public JdbcDataSource removeJdbcDataSourceAt(final int index) {
        Object obj = this.jdbcDataSourceList.remove(index);
        return (JdbcDataSource) obj;
    }

    /**
     * Sets the value of field 'connectionPool'. The field 'connectionPool' has
     * the following description: Database connection pool configuration.
     * 
     * @param connectionPool the value of field 'connectionPool'.
     */
    public void setConnectionPool(final ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * 
     * 
     * @param index
     * @param vJdbcDataSource
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setJdbcDataSource(final int index, final JdbcDataSource vJdbcDataSource) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.jdbcDataSourceList.size()) {
            throw new IndexOutOfBoundsException("setJdbcDataSource: Index value '" + index + "' not in range [0.." + (this.jdbcDataSourceList.size() - 1) + "]");
        }
        
        this.jdbcDataSourceList.set(index, vJdbcDataSource);
    }

    /**
     * 
     * 
     * @param vJdbcDataSourceArray
     */
    public void setJdbcDataSource(final JdbcDataSource[] vJdbcDataSourceArray) {
        //-- copy array
        jdbcDataSourceList.clear();
        
        for (int i = 0; i < vJdbcDataSourceArray.length; i++) {
                this.jdbcDataSourceList.add(vJdbcDataSourceArray[i]);
        }
    }

    /**
     * Sets the value of 'jdbcDataSourceList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vJdbcDataSourceList the Vector to copy.
     */
    public void setJdbcDataSource(final java.util.List<JdbcDataSource> vJdbcDataSourceList) {
        // copy vector
        this.jdbcDataSourceList.clear();
        
        this.jdbcDataSourceList.addAll(vJdbcDataSourceList);
    }

    /**
     * Sets the value of 'jdbcDataSourceList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param jdbcDataSourceList the Vector to set.
     */
    public void setJdbcDataSourceCollection(final java.util.List<JdbcDataSource> jdbcDataSourceList) {
        this.jdbcDataSourceList = jdbcDataSourceList;
    }

}

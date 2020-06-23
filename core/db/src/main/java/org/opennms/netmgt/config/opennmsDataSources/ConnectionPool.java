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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Database connection pool configuration.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "connection-pool")
@XmlAccessorType(XmlAccessType.NONE)
public class ConnectionPool implements java.io.Serializable {

    private static final long serialVersionUID = -5071908810877550391L;

    /**
     * The connection pool implementation to use.
     */
    @XmlAttribute(name = "factory")
    private String factory;

    /**
     * How long, in seconds, an idle connection is kept in the pool before it is
     * removed.
     */
    @XmlAttribute(name = "idleTimeout")
    private Integer idleTimeout;

    /**
     * How long, in seconds, to attempt to make a connection to the database.
     */
    @XmlAttribute(name = "loginTimeout")
    private Integer loginTimeout;

    /**
     * The minimum number of pooled connections to retain.
     */
    @XmlAttribute(name = "minPool")
    private Integer minPool;

    /**
     * The maximum number of pooled connections to retain.
     */
    @XmlAttribute(name = "maxPool")
    private Integer maxPool;

    /**
     * The maximum number of connections that can be created.
     */
    @XmlAttribute(name = "maxSize")
    private Integer maxSize;

    public ConnectionPool() {
        setFactory("org.opennms.core.db.HikariCPConnectionFactory");
    }

    /**
     */
    public void deleteIdleTimeout() {
        this.idleTimeout= null;
    }

    /**
     */
    public void deleteLoginTimeout() {
        this.loginTimeout= null;
    }

    /**
     */
    public void deleteMaxPool() {
        this.maxPool= null;
    }

    /**
     */
    public void deleteMaxSize() {
        this.maxSize= null;
    }

    /**
     */
    public void deleteMinPool() {
        this.minPool= null;
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
        
        if (obj instanceof ConnectionPool) {
            ConnectionPool temp = (ConnectionPool)obj;
            boolean equals = Objects.equals(temp.factory, factory)
                && Objects.equals(temp.idleTimeout, idleTimeout)
                && Objects.equals(temp.loginTimeout, loginTimeout)
                && Objects.equals(temp.minPool, minPool)
                && Objects.equals(temp.maxPool, maxPool)
                && Objects.equals(temp.maxSize, maxSize);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'factory'. The field 'factory' has the following
     * description: The connection pool implementation to use.
     * 
     * @return the value of field 'Factory'.
     */
    public String getFactory() {
        return this.factory;
    }

    /**
     * Returns the value of field 'idleTimeout'. The field 'idleTimeout' has the
     * following description: How long, in seconds, an idle connection is kept in
     * the pool before it is removed.
     * 
     * @return the value of field 'IdleTimeout'.
     */
    public Integer getIdleTimeout() {
        return this.idleTimeout != null ? this.idleTimeout : Integer.valueOf("600");
    }

    /**
     * Returns the value of field 'loginTimeout'. The field 'loginTimeout' has the
     * following description: How long, in seconds, to attempt to make a
     * connection to the database.
     * 
     * @return the value of field 'LoginTimeout'.
     */
    public Integer getLoginTimeout() {
        return this.loginTimeout != null ? this.loginTimeout : Integer.valueOf("3");
    }

    /**
     * Returns the value of field 'maxPool'. The field 'maxPool' has the following
     * description: The maximum number of pooled connections to retain.
     * 
     * @return the value of field 'MaxPool'.
     */
    public Integer getMaxPool() {
        return this.maxPool != null ? this.maxPool : Integer.valueOf("50");
    }

    /**
     * Returns the value of field 'maxSize'. The field 'maxSize' has the following
     * description: The maximum number of connections that can be created.
     * 
     * @return the value of field 'MaxSize'.
     */
    public Integer getMaxSize() {
        return this.maxSize != null ? this.maxSize : Integer.valueOf("500");
    }

    /**
     * Returns the value of field 'minPool'. The field 'minPool' has the following
     * description: The minimum number of pooled connections to retain.
     * 
     * @return the value of field 'MinPool'.
     */
    public Integer getMinPool() {
        return this.minPool != null ? this.minPool : Integer.valueOf("10");
    }

    /**
     * Method hasIdleTimeout.
     * 
     * @return true if at least one IdleTimeout has been added
     */
    public boolean hasIdleTimeout() {
        return this.idleTimeout != null;
    }

    /**
     * Method hasLoginTimeout.
     * 
     * @return true if at least one LoginTimeout has been added
     */
    public boolean hasLoginTimeout() {
        return this.loginTimeout != null;
    }

    /**
     * Method hasMaxPool.
     * 
     * @return true if at least one MaxPool has been added
     */
    public boolean hasMaxPool() {
        return this.maxPool != null;
    }

    /**
     * Method hasMaxSize.
     * 
     * @return true if at least one MaxSize has been added
     */
    public boolean hasMaxSize() {
        return this.maxSize != null;
    }

    /**
     * Method hasMinPool.
     * 
     * @return true if at least one MinPool has been added
     */
    public boolean hasMinPool() {
        return this.minPool != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int hash = Objects.hash(
            factory, 
            idleTimeout, 
            loginTimeout, 
            minPool, 
            maxPool, 
            maxSize);
        return hash;
    }

    /**
     * Sets the value of field 'factory'. The field 'factory' has the following
     * description: The connection pool implementation to use.
     * 
     * @param factory the value of field 'factory'.
     */
    public void setFactory(final String factory) {
        this.factory = factory;
    }

    /**
     * Sets the value of field 'idleTimeout'. The field 'idleTimeout' has the
     * following description: How long, in seconds, an idle connection is kept in
     * the pool before it is removed.
     * 
     * @param idleTimeout the value of field 'idleTimeout'.
     */
    public void setIdleTimeout(final Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Sets the value of field 'loginTimeout'. The field 'loginTimeout' has the
     * following description: How long, in seconds, to attempt to make a
     * connection to the database.
     * 
     * @param loginTimeout the value of field 'loginTimeout'.
     */
    public void setLoginTimeout(final Integer loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    /**
     * Sets the value of field 'maxPool'. The field 'maxPool' has the following
     * description: The maximum number of pooled connections to retain.
     * 
     * @param maxPool the value of field 'maxPool'.
     */
    public void setMaxPool(final Integer maxPool) {
        this.maxPool = maxPool;
    }

    /**
     * Sets the value of field 'maxSize'. The field 'maxSize' has the following
     * description: The maximum number of connections that can be created.
     * 
     * @param maxSize the value of field 'maxSize'.
     */
    public void setMaxSize(final Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Sets the value of field 'minPool'. The field 'minPool' has the following
     * description: The minimum number of pooled connections to retain.
     * 
     * @param minPool the value of field 'minPool'.
     */
    public void setMinPool(final Integer minPool) {
        this.minPool = minPool;
    }

}

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
package org.opennms.netmgt.config.opennmsDataSources;


import com.google.common.base.MoreObjects;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.mate.api.EnvironmentScope;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

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
    private String rawIdleTimeout;

    /**
     * How long, in seconds, to attempt to make a connection to the database.
     */
    @XmlAttribute(name = "loginTimeout")
    private String rawLoginTimeout;

    /**
     * The minimum number of pooled connections to retain.
     */
    @XmlAttribute(name = "minPool")
    private String rawMinPool;

    /**
     * The maximum number of pooled connections to retain.
     */
    @XmlAttribute(name = "maxPool")
    private String rawMaxPool;

    /**
     * The maximum number of connections that can be created.
     */
    @XmlAttribute(name = "maxSize")
    private String rawMaxSize;

    public ConnectionPool() {
        setFactory("org.opennms.core.db.HikariCPConnectionFactory");
    }

    /**
     */
    public void deleteIdleTimeout() {
        this.rawIdleTimeout = null;
    }

    /**
     */
    public void deleteLoginTimeout() {
        this.rawLoginTimeout = null;
    }

    /**
     */
    public void deleteMaxPool() {
        this.rawMaxPool = null;
    }

    /**
     */
    public void deleteMaxSize() {
        this.rawMaxSize = null;
    }

    /**
     */
    public void deleteMinPool() {
        this.rawMinPool = null;
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
                && Objects.equals(temp.rawIdleTimeout, rawIdleTimeout)
                && Objects.equals(temp.rawLoginTimeout, rawLoginTimeout)
                && Objects.equals(temp.rawMinPool, rawMinPool)
                && Objects.equals(temp.rawMaxPool, rawMaxPool)
                && Objects.equals(temp.rawMaxSize, rawMaxSize);
            return equals;
        }
        return false;
    }

    private String interpolateAttribute(final String value) {
        return interpolateAttribute(value, JCEKSSecureCredentialsVault.defaultScv());
    }

    private String interpolateAttribute(final String value, final SecureCredentialsVault secureCredentialsVault) {
        if (value == null) {
            return null;
        }
        final Interpolator.Result result = Interpolator.interpolate(value,
            new FallbackScope(
                new SecureCredentialsVaultScope(secureCredentialsVault),
                new EnvironmentScope()
            ));
        return result.output;
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
        String interpolated = interpolateAttribute(this.rawIdleTimeout);
        return interpolated != null ? Integer.valueOf(interpolated) : Integer.valueOf("600");
    }

    /**
     * Returns the value of field 'loginTimeout'. The field 'loginTimeout' has the
     * following description: How long, in seconds, to attempt to make a
     * connection to the database.
     *
     * @return the value of field 'LoginTimeout'.
     */
    public Integer getLoginTimeout() {
        String interpolated = interpolateAttribute(this.rawLoginTimeout);
        return interpolated != null ? Integer.valueOf(interpolated) : Integer.valueOf("3");
    }

    /**
     * Returns the value of field 'maxPool'. The field 'maxPool' has the following
     * description: The maximum number of pooled connections to retain.
     *
     * @return the value of field 'MaxPool'.
     */
    public Integer getMaxPool() {
        String interpolated = interpolateAttribute(this.rawMaxPool);
        return interpolated != null ? Integer.valueOf(interpolated) : Integer.valueOf("50");
    }

    /**
     * Returns the value of field 'maxSize'. The field 'maxSize' has the following
     * description: The maximum number of connections that can be created.
     *
     * @return the value of field 'MaxSize'.
     */
    public Integer getMaxSize() {
        String interpolated = interpolateAttribute(this.rawMaxSize);
        return interpolated != null ? Integer.valueOf(interpolated) : Integer.valueOf("500");
    }

    /**
     * Returns the value of field 'minPool'. The field 'minPool' has the following
     * description: The minimum number of pooled connections to retain.
     *
     * @return the value of field 'MinPool'.
     */
    public Integer getMinPool() {
        String interpolated = interpolateAttribute(this.rawMinPool);
        return interpolated != null ? Integer.valueOf(interpolated) : Integer.valueOf("10");
    }

    /**
     * Method hasIdleTimeout.
     *
     * @return true if at least one IdleTimeout has been added
     */
    public boolean hasIdleTimeout() {
        return this.rawIdleTimeout != null;
    }

    /**
     * Method hasLoginTimeout.
     *
     * @return true if at least one LoginTimeout has been added
     */
    public boolean hasLoginTimeout() {
        return this.rawLoginTimeout != null;
    }

    /**
     * Method hasMaxPool.
     *
     * @return true if at least one MaxPool has been added
     */
    public boolean hasMaxPool() {
        return this.rawMaxPool != null;
    }

    /**
     * Method hasMaxSize.
     *
     * @return true if at least one MaxSize has been added
     */
    public boolean hasMaxSize() {
        return this.rawMaxSize != null;
    }

    /**
     * Method hasMinPool.
     *
     * @return true if at least one MinPool has been added
     */
    public boolean hasMinPool() {
        return this.rawMinPool != null;
    }

    /**
     * Method hashCode.
     *
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int hash = Objects.hash(
            factory,
            rawIdleTimeout,
            rawLoginTimeout,
            rawMinPool,
            rawMaxPool,
            rawMaxSize);
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
        this.rawIdleTimeout = idleTimeout != null ? idleTimeout.toString() : null;
    }

    /**
     * Sets the value of field 'idleTimeout'. The field 'idleTimeout' has the
     * following description: How long, in seconds, an idle connection is kept in
     * the pool before it is removed.
     *
     * @param idleTimeout the raw string value of field 'idleTimeout'.
     */
    public void setIdleTimeout(final String idleTimeout) {
        this.rawIdleTimeout = idleTimeout;
    }

    /**
     * Sets the value of field 'loginTimeout'. The field 'loginTimeout' has the
     * following description: How long, in seconds, to attempt to make a
     * connection to the database.
     *
     * @param loginTimeout the value of field 'loginTimeout'.
     */
    public void setLoginTimeout(final Integer loginTimeout) {
        this.rawLoginTimeout = loginTimeout != null ? loginTimeout.toString() : null;
    }

    /**
     * Sets the value of field 'loginTimeout'. The field 'loginTimeout' has the
     * following description: How long, in seconds, to attempt to make a
     * connection to the database.
     *
     * @param loginTimeout the raw string value of field 'loginTimeout'.
     */
    public void setLoginTimeout(final String loginTimeout) {
        this.rawLoginTimeout = loginTimeout;
    }

    /**
     * Sets the value of field 'maxPool'. The field 'maxPool' has the following
     * description: The maximum number of pooled connections to retain.
     *
     * @param maxPool the value of field 'maxPool'.
     */
    public void setMaxPool(final Integer maxPool) {
        this.rawMaxPool = maxPool != null ? maxPool.toString() : null;
    }

    /**
     * Sets the value of field 'maxPool'. The field 'maxPool' has the following
     * description: The maximum number of pooled connections to retain.
     *
     * @param maxPool the raw string value of field 'maxPool'.
     */
    public void setMaxPool(final String maxPool) {
        this.rawMaxPool = maxPool;
    }

    /**
     * Sets the value of field 'maxSize'. The field 'maxSize' has the following
     * description: The maximum number of connections that can be created.
     *
     * @param maxSize the value of field 'maxSize'.
     */
    public void setMaxSize(final Integer maxSize) {
        this.rawMaxSize = maxSize != null ? maxSize.toString() : null;
    }

    /**
     * Sets the value of field 'maxSize'. The field 'maxSize' has the following
     * description: The maximum number of connections that can be created.
     *
     * @param maxSize the raw string value of field 'maxSize'.
     */
    public void setMaxSize(final String maxSize) {
        this.rawMaxSize = maxSize;
    }

    /**
     * Sets the value of field 'minPool'. The field 'minPool' has the following
     * description: The minimum number of pooled connections to retain.
     *
     * @param minPool the value of field 'minPool'.
     */
    public void setMinPool(final Integer minPool) {
        this.rawMinPool = minPool != null ? minPool.toString() : null;
    }

    /**
     * Sets the value of field 'minPool'. The field 'minPool' has the following
     * description: The minimum number of pooled connections to retain.
     *
     * @param minPool the raw string value of field 'minPool'.
     */
    public void setMinPool(final String minPool) {
        this.rawMinPool = minPool;
    }

    public static ConnectionPool merge(final ConnectionPool a,
                                       final ConnectionPool b) {
        if (b == null) {
            return a;
        }

        if (a == null) {
            return b;
        }

        final var pool = new ConnectionPool();
        pool.factory = MoreObjects.firstNonNull(a.factory, b.factory);
        pool.rawIdleTimeout = MoreObjects.firstNonNull(a.rawIdleTimeout, b.rawIdleTimeout);
        pool.rawLoginTimeout = MoreObjects.firstNonNull(a.rawLoginTimeout, b.rawLoginTimeout);
        pool.rawMinPool = MoreObjects.firstNonNull(a.rawMinPool, b.rawMinPool);
        pool.rawMaxPool = MoreObjects.firstNonNull(a.rawMaxPool, b.rawMaxPool);
        pool.rawMaxSize = MoreObjects.firstNonNull(a.rawMaxSize, b.rawMaxSize);

        return pool;
    }

}

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
package org.opennms.core.db;

import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class BaseConnectionFactory implements ClosableDataSource {
	
	private static final Logger LOG = LoggerFactory.getLogger(BaseConnectionFactory.class);

    /**
     * @param stream A configuration file as an {@link InputStream}.
     * @param dsName The data source's name.
     * @throws java.beans.PropertyVetoException if any.
     * @throws java.sql.SQLException if any.
     */
    protected BaseConnectionFactory(final JdbcDataSource ds) throws SQLException {
        initializePool(ds);
    }

    protected abstract void initializePool(final JdbcDataSource ds) throws SQLException;

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getUrl();

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link java.lang.String} object.
     */
    public abstract void setUrl(final String url);

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getUser();

    /**
     * <p>setUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public abstract void setUser(final String user);

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public abstract DataSource getDataSource();

    /**
     * <p>getLoginTimeout</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public abstract int getLoginTimeout() throws SQLException;

    /**
     * <p>close</p>
     */
    @Override
    public void close() {
    }

    /**
     * <p>unwrap</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;  //TODO
    }

    /**
     * <p>isWrapperFor</p>
     *
     * @param iface a {@link java.lang.Class} object.
     * @return a boolean.
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false; //TODO
    }

    protected static void validateJdbcUrl(String url) {
        try {
            if (url == null) {
                throw new IllegalArgumentException("Null JDBC URL");
            } else if (url.length() == 0) {
                throw new IllegalArgumentException("Blank JDBC URL");
            } else if (url.matches("\\$\\{.*\\}")) {
                throw new IllegalArgumentException("JDBC URL cannot contain replacement tokens");
            }
        } catch (IllegalArgumentException e) {
        	LOG.error("Invalid JDBC URL specified: {}", e.getMessage(), e);
            throw e;
        }
    }
}

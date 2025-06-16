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
package org.opennms.core.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility for tracking open JDBC {@link Statement}s, {@link ResultSet}s, and {@link Connection}s
 * to ease cleanup and avoid connection leaks.
 *
 * @author Benjamin Reed &lt;ranger@opennms.org&gt;
 */
public class DBUtils {
    private Logger LOG = LoggerFactory.getLogger(DBUtils.class);

    private final Set<Statement> m_statements;
    private final Set<ResultSet> m_resultSets;
    private final Set<Connection> m_connections;

    /**
     * <p>Constructor for DBUtils.</p>
     */
    public DBUtils() {
        this(DBUtils.class);
    }

    /**
     * <p>Constructor for DBUtils.</p>
     *
     * @param loggingClass a {@link java.lang.Class} object.
     */
    public DBUtils(Class<?> loggingClass) {
        m_statements = Collections.synchronizedSet(new HashSet<Statement>());
        m_resultSets = Collections.synchronizedSet(new HashSet<ResultSet>());
        m_connections = Collections.synchronizedSet(new HashSet<Connection>());
        LOG = LoggerFactory.getLogger(loggingClass);
    }

    public DBUtils(Class<?> loggingClass, Object... targets) {
        this(loggingClass);
        for (final Object o : targets) {
            watch(o);
        }
    }
    /**
     * <p>setLoggingClass</p>
     *
     * @param c a {@link java.lang.Class} object.
     * @return a {@link org.opennms.core.utils.DBUtils} object.
     */
    public DBUtils setLoggingClass(Class<?> c) {
        LOG = LoggerFactory.getLogger(c);
        return this;
    }

    /**
     * <p>watch</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a {@link org.opennms.core.utils.DBUtils} object.
     */
    public DBUtils watch(Object o) {
        if (o instanceof Statement) {
            m_statements.add((Statement)o);
        } else if (o instanceof ResultSet) {
            m_resultSets.add((ResultSet)o);
        } else if (o instanceof Connection) {
            m_connections.add((Connection)o);
        }
        return this;
    }

    /**
     * <p>cleanUp</p>
     */
    public void cleanUp() {
        for (ResultSet rs : m_resultSets) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    LOG.warn("Unable to close result set", e);
                }
            }
        }
        m_resultSets.clear();

        for (Statement s : m_statements) {
            if (s != null) {
                try {
                    s.close();
                } catch (Throwable e) {
                    LOG.warn("Unable to close statement", e);
                }
            }
        }
        m_statements.clear();

        for (Connection c : m_connections) {
            if (c != null) {
                try {
                    c.close();
                } catch (Throwable e) {
                    LOG.warn("Unable to close connection", e);
                }
            }
        }
        m_connections.clear();
    }

}

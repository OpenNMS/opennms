/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
 * <p>DBUtils class.</p>
 *
 * @author ranger
 */
public class DBUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(DBUtils.class);
	
    private final Set<Statement> m_statements;
    private final Set<ResultSet> m_resultSets;
    private final Set<Connection> m_connections;
    private Class<?> m_loggingClass;

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
        m_loggingClass = loggingClass;
    }

    /**
     * <p>setLoggingClass</p>
     *
     * @param c a {@link java.lang.Class} object.
     * @return a {@link org.opennms.core.utils.DBUtils} object.
     */
    public DBUtils setLoggingClass(Class<?> c) {
        m_loggingClass = c;
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

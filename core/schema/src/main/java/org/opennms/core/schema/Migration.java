/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.schema;

import liquibase.resource.ResourceAccessor;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * <p>Migration class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Migration {
    private String m_jdbcUrl;
    private String m_jdbcDriver = "org.postgresql.Driver";
    private String m_databaseHost;
    private String m_databaseName;
    private String m_schemaName;
    private String m_databaseUser;
    private String m_databasePassword;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_changeLog;
    private ResourceAccessor m_accessor;

    /**
     * Get the JDBC connection URL.  Defaults to jdbc:postgresql://host/database.
     *
     * @return the JDBC URL
     */
    public String getJdbcUrl() {
        if (m_jdbcUrl == null) {
               return String.format("jdbc:postgresql://%s/%s", getDatabaseHost(), getDatabaseName()); 
        }
        return m_jdbcUrl;
    }
    /**
     * <p>setJdbcUrl</p>
     *
     * @param jdbcUrl a {@link java.lang.String} object.
     */
    public void  setJdbcUrl(String jdbcUrl) {
        m_jdbcUrl = jdbcUrl;
    }

    /**
     * Get the JDBC driver class name.  Defaults to "org.postgresql.Driver"
     *
     * @return the class name
     */
    public String getJdbcDriver() {
        return m_jdbcDriver;
    }
    /**
     * <p>setJdbcDriver</p>
     *
     * @param jdbcDriver a {@link java.lang.String} object.
     */
    public void setJdbcDriver(String jdbcDriver) {
        m_jdbcDriver = jdbcDriver;
    }

    /**
     * <p>getDatabaseHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabaseHost() {
        return m_databaseHost;
    }
    /**
     * <p>setDatabaseHost</p>
     *
     * @param databaseHost a {@link java.lang.String} object.
     */
    public void setDatabaseHost(String databaseHost) {
        m_databaseHost = databaseHost;
    }

    /**
     * <p>getDatabaseName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabaseName() {
        return m_databaseName;
    }
    /**
     * <p>setDatabaseName</p>
     *
     * @param databaseName a {@link java.lang.String} object.
     */
    public void setDatabaseName(String databaseName) {
        m_databaseName = databaseName;
    }

    public String getSchemaName() {
    	return m_schemaName;
    }
    
    public void setSchemaName(final String schemaName) {
    	m_schemaName = schemaName;
    }

    /**
     * <p>getDatabaseUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabaseUser() {
        return m_databaseUser;
    }
    /**
     * <p>setDatabaseUser</p>
     *
     * @param databaseUser a {@link java.lang.String} object.
     */
    public void setDatabaseUser(String databaseUser) {
        m_databaseUser = databaseUser;
    }

    /**
     * <p>getDatabasePassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabasePassword() {
        return m_databasePassword;
    }
    /**
     * <p>setDatabasePassword</p>
     *
     * @param databasePassword a {@link java.lang.String} object.
     */
    public void setDatabasePassword(String databasePassword) {
        m_databasePassword = databasePassword;
    }

    /**
     * <p>getAdminUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdminUser() {
        return m_adminUser;
    }
    /**
     * <p>setAdminUser</p>
     *
     * @param adminUser a {@link java.lang.String} object.
     */
    public void setAdminUser(String adminUser) {
        m_adminUser = adminUser;
    }

    /**
     * <p>getAdminPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdminPassword() {
        return m_adminPassword;
    }
    /**
     * <p>setAdminPassword</p>
     *
     * @param adminPassword a {@link java.lang.String} object.
     */
    public void setAdminPassword(String adminPassword) {
        m_adminPassword = adminPassword;
    }

    /**
     * <p>getChangeLog</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getChangeLog() {
        return m_changeLog;
    }
    /**
     * <p>setChangeLog</p>
     *
     * @param changeLog a {@link java.lang.String} object.
     */
    public void setChangeLog(String changeLog) {
        m_changeLog = changeLog;
    }
    
    public ResourceAccessor getAccessor() {
        return m_accessor;
    }

    public void setAccessor(final ResourceAccessor accessor) {
        m_accessor = accessor;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("database", m_databaseName)
            .append("schema", m_schemaName)
            .append("host", m_databaseHost)
            .append("driver", m_jdbcDriver)
            .append("url", m_jdbcUrl)
            .append("admin-user", m_adminUser)
            .append("user", m_databasePassword)
            .append("changelog", m_changeLog)
            .append("accessor", m_accessor)
            .toString();
    }
}

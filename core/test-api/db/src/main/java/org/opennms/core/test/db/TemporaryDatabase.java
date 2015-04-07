/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test.db;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public interface TemporaryDatabase extends DataSource, XADataSource {
    public static final String TEST_DB_NAME_PREFIX = "opennms_test_";
    public static final String DRIVER_PROPERTY = "mock.db.driver";
    public static final String URL_PROPERTY = "mock.db.url";
    public static final String ADMIN_USER_PROPERTY = "mock.db.adminUser";
    public static final String ADMIN_PASSWORD_PROPERTY = "mock.db.adminPassword";
    public static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    public static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    public static final String DEFAULT_ADMIN_USER = "postgres";
    public static final String DEFAULT_ADMIN_PASSWORD = "";

    public String getTestDatabase();
    public void setPopulateSchema(boolean populate);
    public void create() throws TemporaryDatabaseException;
    public void drop() throws TemporaryDatabaseException;
    public int countRows(final String sql, Object... values);
    public JdbcTemplate getJdbcTemplate();
}

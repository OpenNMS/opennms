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
package org.opennms.core.test.db;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public interface TemporaryDatabase extends DataSource, XADataSource {
    public static final String DRIVER_PROPERTY = "mock.db.driver";
    public static final String URL_PROPERTY = "mock.db.url";
    public static final String ADMIN_USER_PROPERTY = "mock.db.adminUser";
    public static final String ADMIN_PASSWORD_PROPERTY = "mock.db.adminPassword";
    public static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    public static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    public static final String DEFAULT_ADMIN_USER = "postgres";
    public static final String DEFAULT_ADMIN_PASSWORD = "";

    public void setPopulateSchema(boolean populate);
    public void setPlpgsqlIplike(boolean plpgsqlIplike);

    public void setClassName(String string);
    public void setMethodName(String string);
    public void setTestDetails(String string);

    public void create() throws TemporaryDatabaseException;
    public void drop() throws TemporaryDatabaseException;

    public String getTestDatabase();
    public JdbcTemplate getJdbcTemplate();
    public int countRows(final String sql, Object... values);
}

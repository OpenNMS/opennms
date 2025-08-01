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
package org.opennms.core.test.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabasePostgreSQL;

/**
 * JUnitTemporaryDatabase
 *
 * @author brozow
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface JUnitTemporaryDatabase {
	/**
	 * Create the standard OpenNMS schema in the test database.
	 *
	 * @return
	 */
    boolean createSchema() default true;

    /**
     * Use the specified database instead of a temporary database.
     * Use this if you need to examine data in the database during (e.g.: in a debugger) and/or after a test.
     * The specified database will not be deleted after the test.
     *
     * @return
     */
    String useExistingDatabase() default "";

    /**
     * Use a specific class for creating temporary databases.
     * @return
     */
    Class<? extends TemporaryDatabase> tempDbClass() default TemporaryDatabasePostgreSQL.class;

    /**
     * Reuse the database for all tests in a class.
     * A new database is created for every test method and destroyed after every test, regardless
     * of this setting.
     * Ignored if useExistingDatabase is set (for both database creation and destruction).
     *
     * @return
     */
    boolean reuseDatabase() default true;

    /**
     * Mark the ApplicationContext dirty after a test method executes.
     * After the last test method in a class, the ApplicationContext is always marked dirty, regardless of this setting.
     *
     * @return
     */
    boolean dirtiesContext() default true;

    /**
     * Whether to use a connection pool or not. Defaults to true.
     * @return
     */
    boolean poolConnections() default true;

    /**
     * Whether to force using the pl/pgsql version of IPLIKE.
     * @return
     */
    boolean plpgsqlIplike() default false;
}

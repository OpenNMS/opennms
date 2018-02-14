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
}

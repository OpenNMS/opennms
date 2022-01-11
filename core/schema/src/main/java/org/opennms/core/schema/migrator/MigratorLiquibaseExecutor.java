/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.core.schema.migrator;

import liquibase.Contexts;
import liquibase.exception.LiquibaseException;
import org.opennms.core.schema.MigrationException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

public interface MigratorLiquibaseExecutor {
    /**
     * Perform the Liquibase update using the provided contexts (optional).
     *
     * @param changelog URI or path to the changelog to apply.
     * @param contexts optional contexts to apply; may be null to use the liquibase "no context mode".
     * @param dataSource data source used to access the database for the migration
     * @param schemaName name of the database schema against which the updates will be made
     * @param changeLogParameters change log parameters for liquibase
     * @throws MigrationException
     */
    void update(
            String changelog,
            String contexts,
            DataSource dataSource,
            String schemaName,
            Map<String, String> changeLogParameters
    ) throws MigrationException, SQLException, LiquibaseException;
}

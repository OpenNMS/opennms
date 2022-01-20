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

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.util.Map;

public class SpringContextBasedLiquibaseExecutor implements MigratorLiquibaseExecutor {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(SpringContextBasedLiquibaseExecutor.class);

    private Logger log = DEFAULT_LOGGER;

    private final ApplicationContext m_applicationContext;

//========================================
// Constructors
//========================================

    /**
     * Use the given Spring Application Context to locate resources.
     *
     * @param applicationContext
     */
    public SpringContextBasedLiquibaseExecutor(ApplicationContext applicationContext) {
        this.m_applicationContext = applicationContext;
    }


//========================================
// Interface
//========================================

    @Override
    public void update(
            String changelog,
            String contexts,
            DataSource datasource,
            String schemaName,
            Map<String, String> changeLogParameters
    ) throws LiquibaseException {

        SpringLiquibase liquibase = this.prepareLiquibase(datasource, schemaName, changelog, contexts, changeLogParameters);

        liquibase.afterPropertiesSet();
    }

//========================================
// Internals
//========================================

    private SpringLiquibase prepareLiquibase(
            DataSource datasource,
            String schemaName,
            String changelogUri,
            String contexts,
            Map<String, String> changelogParameters
    ) throws LiquibaseException {

        SpringLiquibase liquibase = new SpringLiquibase();

        liquibase.setResourceLoader(this.m_applicationContext);
        liquibase.setChangeLog(changelogUri);
        liquibase.setDataSource(datasource);
        liquibase.setChangeLogParameters(changelogParameters);
        liquibase.setDefaultSchema(schemaName);
        liquibase.setContexts(contexts);
        liquibase.afterPropertiesSet();

        return liquibase;
    }
}

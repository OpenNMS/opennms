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

import org.opennms.core.schema.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ClassLoaderBasedMigratorResourceProvider implements MigratorResourceProvider {
    public static final String LIQUIBASE_CHANGELOG_FILENAME = "changelog.xml";

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(ClassLoaderBasedMigratorResourceProvider.class);

    private Logger log = DEFAULT_LOGGER;


    private final ClassLoader m_classLoader;

//========================================
// Constructors
//========================================

    public ClassLoaderBasedMigratorResourceProvider() {
        this.m_classLoader = this.getClass().getClassLoader();
    }

    public ClassLoaderBasedMigratorResourceProvider(ClassLoader m_classLoader) {
        this.m_classLoader = m_classLoader;
    }


//========================================
// Interface
//========================================

    @Override
    public Collection<String> getLiquibaseChangelogs(boolean required) throws Exception {
        List<String> located = new LinkedList<>();

        URL url = this.m_classLoader.getResource(LIQUIBASE_CHANGELOG_FILENAME);
        if (url != null) {
            located.add(url.toString());
        }

        if (( required ) && ( located.isEmpty() )) {
            throw new MigrationException("Could not find the '" + LIQUIBASE_CHANGELOG_FILENAME + "' file.");
        }

        return located;
    }

}

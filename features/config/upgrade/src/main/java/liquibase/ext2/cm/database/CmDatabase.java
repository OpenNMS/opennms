/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package liquibase.ext2.cm.database;

import java.util.Objects;

import org.opennms.features.config.service.api.ConfigurationManagerService;

import liquibase.database.core.PostgresDatabase;

/**
 * We set a dummy database here since we are not actually modifying a database but sending
 * instructions to the ConfigurationManager.
 */
public class CmDatabase extends PostgresDatabase {
    public static final String PRODUCT_NAME = "cm";
    public static final String PRODUCT_SHORT_NAME = "cm";

    final ConfigurationManagerService configurationManager;

    public CmDatabase(final ConfigurationManagerService configurationManager) {
        this.configurationManager = Objects.requireNonNull(configurationManager);
    }

    @Override
    public String getDatabaseProductName() {
        return PRODUCT_NAME;
    }

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    @Override
    public String getShortName() {
        return PRODUCT_SHORT_NAME;
    }


    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    public ConfigurationManagerService getConfigurationManager() {
        return this.configurationManager;
    }

}

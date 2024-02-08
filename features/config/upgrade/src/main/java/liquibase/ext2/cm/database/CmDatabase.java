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

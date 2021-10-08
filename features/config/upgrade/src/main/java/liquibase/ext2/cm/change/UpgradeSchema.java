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

package liquibase.ext2.cm.change;

import org.opennms.features.config.service.api.ConfigurationManagerService;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;

/** Used in changelog.xml */
@DatabaseChange(name = "upgradeSchema", description = "Upgrades a new schema", priority = ChangeMetaData.PRIORITY_DATABASE)
public class UpgradeSchema extends AbstractSchemaChange {

    protected String getChangeName() {
        return "Upgrade";
    }

    protected RunnableWithException getCmFunction(ConfigurationManagerService m) {
        return () -> m.upgradeSchema(id, xsdName, this.rootElement);
    }
}



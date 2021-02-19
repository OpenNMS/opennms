/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.cm.svc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChangelogManagerImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void doIt() {
        /*
        // Setup
        SchemaManager schemaManager = new SchemaManagerSvc();
        ConfigStore configStore = new ConfigStoreSvc();

        ChangelogManagerImpl cm = new ChangelogManagerImpl();
        cm.setHome(temporaryFolder.getRoot().getAbsolutePath());
        cm.setEmbeddedChangelogPath("classpath:/test/test-changelog.xml");
        cm.setJsonKvStore(null);
        cm.setSchemaManager(schemaManager);
        cm.setConfigStore(configStore);

        cm.applyEmbeddedChangelog();
         */

        /*
        assertThat(schemaManager.getServiceNames(), containsInAnyOrder("vacuumd", "sysprops", "amqp-forwarder"));
        assertThat(schemaManager.getPathsForService("sysprops"), contains(""));
        assertThat(schemaManager.getPathsWithPrefix("sysprops", "/adsf"), contains(""));

        assertThat(schemaManager.getTypeForPath("/sysprops/adsf").getType(), equalTo(ObjectType.class.getName()));
        */

        // cm:set "/ipc/grpc/host" 0
        // cm:get "/asdf" 0
    }
}

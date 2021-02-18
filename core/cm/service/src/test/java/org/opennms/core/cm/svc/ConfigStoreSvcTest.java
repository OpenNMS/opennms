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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;
import org.opennms.core.cm.api.ConfigStore;
import org.opennms.core.cm.api.SchemaManager;
import org.opennms.netmgt.config.vacuumd.Statement;
import org.opennms.netmgt.config.vacuumd.VacuumdConfiguration;

public class ConfigStoreSvcTest {

    private final String VACUUMD_SVC = "vacuumd";
    private final String VACUUMD_XSD = "xsds/vacuumd-configuration.xsd";

    @Test
    public void canRetrieveJaxbObject() throws IOException {
        // Create a new instance of the service
        SchemaManager schemaManager = new SchemaManagerSvc();
        ConfigStore configStore = new ConfigStoreSvc(schemaManager);

        // Attempt to retrieve our configuration should fail, since we haven't registered any model yet
        try {
            configStore.getModel(VACUUMD_SVC, VacuumdConfiguration.class);
            fail("Expecting exception");
        } catch (IllegalStateException e) {
            // pass
        }

        // Register the XSD
        schemaManager.registerXSD(VACUUMD_SVC, VACUUMD_XSD);

        // Retrieve the configuration, should be empty
        Optional<VacuumdConfiguration> vacuumdConfigOpt = configStore.getModel(VACUUMD_SVC, VacuumdConfiguration.class);
        assertThat(vacuumdConfigOpt.isPresent(), equalTo(false));

        // Create a new config.
        VacuumdConfiguration vacuumdConfig = new VacuumdConfiguration();
        Statement stmt = new Statement();
        stmt.setContent("SELECT * FROM events;");
        stmt.setTransactional(true);
        vacuumdConfig.addStatement(stmt);

        // Set the configuration
        configStore.setModel(VACUUMD_SVC, vacuumdConfig);

        // Now try retrieving the configuration again, should match what we set
        vacuumdConfigOpt = configStore.getModel(VACUUMD_SVC, VacuumdConfiguration.class);
        //noinspection OptionalGetWithoutIsPresent
        assertThat(vacuumdConfig, equalTo(vacuumdConfigOpt.get()));
    }
}

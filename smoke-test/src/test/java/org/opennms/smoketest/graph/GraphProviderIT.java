/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.graph;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.test.system.api.NewTestEnvironment;

/**
 * Verifies if exposing a GraphProvider will result in an exposed GraphContainerProvider
 */
public class GraphProviderIT extends OpenNMSSeleniumTestCase {

    private KarafShell karafShell;

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Install features
        karafShell = new KarafShell(getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8101));
        karafShell.runCommand("feature:install opennms-graphs");
    }

    // Here we verify that the graph provider is exposed correctly
    @Test
    public void canExposeGraphProvider() {
        try {
            karafShell.runCommand("feature:install opennms-graph-provider-bsm");
            karafShell.runCommand("feature:list -i", output -> output.contains("opennms-graphs") && output.contains("opennms-graph-provider-bsm"));
            karafShell.runCommand("bsm:generate-hierarchies 5 2");
            karafShell.runCommand("graph:get --container bsm --namespace bsm", output ->
                    output.contains("label => Business Service Graph") && output.contains("Vertex Details (5)"));
        } finally {
            karafShell.runCommand("bsm:delete-generated-hierarchies");
        }
    }

    @Test
    public void canImportGraphRepository() {
        karafShell.runCommand("feature:install opennms-graph-provider-dummy");
        karafShell.runCommand("feature:list -i", output -> output.contains("opennms-graphs") && output.contains("opennms-graph-provider-dummy"));
        karafShell.runCommand("graph:get --container persistent-dummy --namespace persistent-dummy.graph", output -> output.contains("No Vertices"));
    }
}

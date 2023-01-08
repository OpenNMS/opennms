/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2021 The OpenNMS Group, Inc.
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
package org.opennms.smoketest.minion;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.testcontainers.containers.Container;

@Category(MinionTests.class)
public class MinionStartupIT {

    final static Map<String, String> configuration = new TreeMap<>();

    static {
        configuration.put("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616");
        configuration.put("MINION_LOCATION", "Fulda");
        configuration.put("MINION_ID", "Minion-Fulda");
    }

    @ClassRule
    public final static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            // check Minion startup without files in 'opennms.properties.d' directory, see NMS-13347
            .withMinions(new MinionProfile.Builder()
                    .withId(configuration.get("MINION_ID"))
                    .withLocation(configuration.get("MINION_LOCATION"))
                    .withLegacyConfiguration(configuration) // this will prevent any configuration files from being created
                    .build())
            .build());

    @Test
    public void testMinionRunning() throws IOException, InterruptedException {
        assertTrue(stack.minion().isRunning());

        // Do a sanity check and make sure that opennms.properties.d doesn't exist
        var dir = "/opt/minion/etc/opennms.properties.d";
        Container.ExecResult ls = stack.minion().execInContainer("/bin/ls", "-ld", dir);
        assertNotEquals("The directory " + dir + " shouldn't be created in the container"
                        + " when in legacy configuration mode, but 'ls -ld " + dir + "' returned a 0 exit code."
                        + " Output from ls:\n"
                        + ls.getStdout(),
                0, ls.getExitCode());

    }
}

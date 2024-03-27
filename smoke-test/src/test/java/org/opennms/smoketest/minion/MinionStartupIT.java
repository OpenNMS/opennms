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

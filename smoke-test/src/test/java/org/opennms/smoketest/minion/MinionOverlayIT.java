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

import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.TestContainerUtils;

@Category(MinionTests.class)
public class MinionOverlayIT {
    @ClassRule
    public final static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinions(new MinionProfile.Builder()
                    // Copy a random file to the overlay for testing
                    .withFile("empty-discovery-configuration.xml", "random-overlay-test-file")
                    .build())
            .build());

    @Test
    public void testFileOverlay() {
        // This will throw an exception if the file doesn't exist
        TestContainerUtils.getFileFromContainerAsString(stack.minion(),
                Path.of("/opt/minion/etc/random-overlay-test-file"));
    }
}

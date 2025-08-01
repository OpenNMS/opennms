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
package org.opennms.smoketest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.Container;

public class OpenShiftCompatIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
                .withSimulateRestricedOpenShiftEnvironment()
                .build());

    @Test
    public void canStartContainerWithRandomUid() throws IOException, InterruptedException {
        // Verify that the generated UID is in the desired range
        assertThat(stack.opennms().getGeneratedUserId(), greaterThanOrEqualTo(TestContainerUtils.OPENSHIFT_CONTAINER_UID_RANGE_MIN));
        assertThat(stack.opennms().getGeneratedUserId(), lessThanOrEqualTo(TestContainerUtils.OPENSHIFT_CONTAINER_UID_RANGE_MAX));
        // Verify that the effective UID/GID are what is expected
        Container.ExecResult result = stack.opennms().execInContainer("id");
        assertThat(result.getStdout().trim(),
            equalTo(String.format("uid=%d gid=0(root) groups=0(root)", stack.opennms().getGeneratedUserId())));
    }
}

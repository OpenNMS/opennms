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

import java.io.IOException;
import java.time.Duration;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class GrpcExporterPluginIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;
    private static final String FEATURE_GRPC_EXPORTER="opennms-grpc-exporter";
    protected KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Make sure the Karaf shell is healthy before we start
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void everythingHappy() throws Exception {
        karafShell.runCommandOnce("feature:install "+FEATURE_GRPC_EXPORTER ,
               output -> !output.toLowerCase().contains("error"), false);
        karafShell.checkFeature(FEATURE_GRPC_EXPORTER, "Started", Duration.ofSeconds(30));
    }
}

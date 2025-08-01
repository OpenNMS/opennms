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
package org.opennms.smoketest.sentinel;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.BlobStoreStrategy;
import org.opennms.smoketest.stacks.JsonStoreStrategy;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;

@Category(SentinelTests.class)
public class KeyValueStoresIT {
    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withSentinel()
            .withTelemetryProcessing()
            .withBlobStoreStrategy(BlobStoreStrategy.NEWTS_CASSANDRA)
            .withJsonStoreStrategy(JsonStoreStrategy.POSTGRES)
            .withIpcStrategy(IpcStrategy.JMS)
            .build());

    @Test
    public void canPutAndGetBlobsOnSentinel() {
        String value = "blob tubessssssssss!";
        KarafShell ks = new KarafShell(stack.sentinel().getSshAddress());
        ks.runCommand("opennms:kv-put-blob \"key\" \"context\" \"" + value + "\"");
        ks.runCommand("opennms:kv-get-blob \"key\" \"context\"", s -> s.contains(value));
    }

    @Test
    public void canPutAndGetJSONOnSentinel() {
        String value = "{\"label\": \"JSON tubessssssssss!\"}";
        KarafShell ks = new KarafShell(stack.sentinel().getSshAddress());
        ks.runCommand("opennms:kv-put-json \"key\" \"context\" '" + value + "'");
        ks.runCommand("opennms:kv-get-json \"key\" \"context\"", s -> s.contains(value));
    }

    @Test
    public void canPutAndGetJSONOnOpenNMS() {
        String value = "{\"label\": \"JSON tubessssssssss!\"}";
        KarafShell ks = new KarafShell(stack.opennms().getSshAddress());
        ks.runCommand("opennms:kv-put-json \"key\" \"context\" '" + value + "'");
        ks.runCommand("opennms:kv-get-json \"key\" \"context\"", s -> s.contains(value));
    }
}

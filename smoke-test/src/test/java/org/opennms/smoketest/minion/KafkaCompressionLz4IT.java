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

import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.KafkaCompressionStrategy;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;

@Category(MinionTests.class)
public class KafkaCompressionLz4IT extends AbstractKafkaCompressionIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withIpcStrategy(IpcStrategy.KAFKA)
            .withKafkaCompressionStrategy(KafkaCompressionStrategy.LZ4)
            .build());

    public KafkaCompressionLz4IT() {
        super(stack);
    }
}

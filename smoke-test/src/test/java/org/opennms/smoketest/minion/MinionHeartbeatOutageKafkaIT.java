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

import org.junit.experimental.categories.Category;
import org.opennms.smoketest.stacks.IpcStrategy;

/**
 * This test starts up Minion with the Apache Kafka sink and makes sure
 * that heartbeat messages continue to be processed even if the Minion
 * and OpenNMS instances are restarted.
 * 
 * @author Seth
 */
//@Category(MinionTests.class)
@Category(org.opennms.smoketest.junit.FlakyTests.class)
public class MinionHeartbeatOutageKafkaIT extends MinionHeartbeatOutageIT {

    @Override
    public IpcStrategy getIpcStrategy() {
        return IpcStrategy.KAFKA;
    }
}

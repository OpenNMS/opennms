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
package org.opennms.core.ipc.twin.memory;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.core.ipc.twin.test.AbstractTwinBrokerIT;
import org.opennms.distributed.core.api.MinionIdentity;

public class MemoryTwinBrokerIT extends AbstractTwinBrokerIT {

    // This is a special case: The in-memory broker is assumed to be a singleton for the application lifetime. As the
    // subscriptions can be local only, they are expected to be lost on a restart.
    private final MemoryTwinPublisher broker = new MemoryTwinPublisher();

    @Override
    protected TwinPublisher createPublisher() {
        return this.broker;
    }

    @Override
    protected TwinSubscriber createSubscriber(final MinionIdentity identity) {
        return new MemoryTwinSubscriber(this.broker, identity.getLocation());
    }
}

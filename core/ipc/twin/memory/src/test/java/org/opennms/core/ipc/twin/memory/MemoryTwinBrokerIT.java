/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
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

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

package org.opennms.core.ipc.twin.common;


import com.codahale.metrics.MetricRegistry;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.Identity;

public class LocalTwinSubscriberImpl extends AbstractTwinSubscriber implements LocalTwinSubscriber {

    public LocalTwinSubscriberImpl(final Identity identity, TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        super(identity, tracerRegistry, metricRegistry);
    }

    @Override
    protected void sendRpcRequest(TwinRequest twinRequest) {
        // no need to send any RPC on local subscriber.
    }

    @Override
    public void accept(TwinUpdate twinUpdate) {
        super.accept(twinUpdate);
    }

    @Override
    public TracerRegistry getTracerRegistry() {
        return super.getTracerRegistry();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return super.getMetrics();
    }
}



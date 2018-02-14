/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.camel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;

import com.codahale.metrics.JmxReporter;

/**
 * Message dispatcher that sends messages via JMS.
 *
 * @author jwhite
 */
public class CamelRemoteMessageDispatcherFactory extends AbstractMessageDispatcherFactory<Map<String, Object>> {

    @EndpointInject(uri = "direct:sendMessage", context = "sinkClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:sendMessage", context = "sinkClient")
    private Endpoint endpoint;

    private JmxReporter reporter;

    public <S extends Message, T extends Message> Map<String, Object> getModuleMetadata(SinkModule<S, T> module) {
        // Pre-compute the JMS headers instead of recomputing them every dispatch
        final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(
                CamelSinkConstants.JMS_QUEUE_PREFIX, module.getId());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CamelSinkConstants.JMS_QUEUE_NAME_HEADER, queueNameFactory.getName());
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Map<String, Object> headers, T message) {
        template.sendBodyAndHeaders(endpoint, module.marshal((T)message), headers);
    }

    public void registerJmxReporter() {
        reporter = JmxReporter.forRegistry(getMetrics())
                .inDomain(CamelLocalMessageDispatcherFactory.class.getPackage().getName())
                .build();
        reporter.start();
    }

    public void unregisterJmxReporter() {
        if (reporter != null) {
            reporter.close();
            reporter = null;
        }
    }
}

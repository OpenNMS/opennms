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

import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;

public class CamelSinkServerProcessor implements Processor {

    private final CamelMessageConsumerManager consumerManager;
    private final SinkModule<?, Message> module;

    public CamelSinkServerProcessor(CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module) {
        this.consumerManager = Objects.requireNonNull(consumerManager);
        this.module = Objects.requireNonNull(module);
    }

    @Override
    public void process(Exchange exchange) {
        final byte[] messageBytes = exchange.getIn().getBody(byte[].class);
        final Message message = module.unmarshal(messageBytes);
        consumerManager.dispatch(module, message);
    }
}

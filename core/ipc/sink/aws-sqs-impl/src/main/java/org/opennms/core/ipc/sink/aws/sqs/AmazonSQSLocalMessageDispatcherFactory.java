/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.JmxReporter;

/**
 * Dispatches the messages directly the consumers.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSLocalMessageDispatcherFactory extends AbstractMessageDispatcherFactory<Void> implements InitializingBean {

    /** The message consumer manager. */
    @Autowired
    private AmazonSQSMessageConsumerManager messageConsumerManager;

    /* (non-Javadoc)
     * @see org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory#dispatch(org.opennms.core.ipc.sink.api.SinkModule, java.lang.Object, org.opennms.core.ipc.sink.api.Message)
     */
    public <S extends Message, T extends Message> void dispatch(final SinkModule<S, T> module, final Void metadata, final T message) {
        messageConsumerManager.dispatch(module, message);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final JmxReporter reporter = JmxReporter.forRegistry(getMetrics())
                .inDomain(AmazonSQSLocalMessageDispatcherFactory.class.getPackage().getName())
                .build();
        reporter.start();
    }
}

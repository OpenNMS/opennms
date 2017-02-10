/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.common;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.test.ThreadLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link MessageConsumer} is used to verify the number of threads
 * that are consuming messages.
 *
 * @author jwhite
 */
public class ThreadLockingMessageConsumer<S extends Message, T extends Message> extends ThreadLocker implements MessageConsumer<S, T> {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadLockingMessageConsumer.class);

    private final SinkModule<S, T> module;

    public ThreadLockingMessageConsumer(SinkModule<S, T> module) {
        this.module = Objects.requireNonNull(module);
    }

    @Override
    public SinkModule<S, T> getModule() {
        return module;
    }

    @Override
    public void handleMessage(final T message) {
        LOG.debug("handling message: {} ({} extra threads waiting)", message, getNumExtraThreadsWaiting());
        park();
    }
}

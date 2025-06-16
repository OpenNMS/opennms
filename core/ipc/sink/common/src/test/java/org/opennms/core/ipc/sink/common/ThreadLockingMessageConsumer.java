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

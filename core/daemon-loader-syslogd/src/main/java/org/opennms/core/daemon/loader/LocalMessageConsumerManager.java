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
package org.opennms.core.daemon.loader;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-process MessageConsumerManager for standalone daemon containers.
 *
 * Messages dispatched via {@link LocalMessageDispatcherFactory} are delivered
 * directly to consumers registered here — no remote transport needed.
 */
public class LocalMessageConsumerManager extends AbstractMessageConsumerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMessageConsumerManager.class);

    @Override
    protected void startConsumingForModule(org.opennms.core.ipc.sink.api.SinkModule<?, Message> module) {
        LOG.info("Local sink consumer started for module: {}", module.getId());
    }

    @Override
    protected void stopConsumingForModule(org.opennms.core.ipc.sink.api.SinkModule<?, Message> module) {
        LOG.info("Local sink consumer stopped for module: {}", module.getId());
    }
}

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
package org.opennms.netmgt.syslogd;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SinkDispatchingSyslogReceiver implements SyslogReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(SinkDispatchingSyslogReceiver.class);

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private MessageDispatcherFactory m_messageDispatcherFactory;

    private final SyslogdConfig m_config;

    protected AsyncDispatcher<SyslogConnection> m_dispatcher;

    public SinkDispatchingSyslogReceiver(SyslogdConfig config) {
        m_config = Objects.requireNonNull(config);
    }

    @Override
    public void run() {
        // Set the prefix for this thread
        Logging.putPrefix(Syslogd.LOG4J_CATEGORY);

        // Create an asynchronous dispatcher
        final SyslogSinkModule syslogSinkModule = new SyslogSinkModule(m_config, m_distPollerDao);
        m_dispatcher = m_messageDispatcherFactory.createAsyncDispatcher(syslogSinkModule);
    }

    @Override
    public void stop() throws InterruptedException {
        try {
            if (m_dispatcher != null) {
                m_dispatcher.close();
                m_dispatcher = null;
            }
        } catch (Exception e) {
            LOG.warn("Exception while closing dispatcher.", e);
        }
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        m_messageDispatcherFactory = messageDispatcherFactory;
    }
}

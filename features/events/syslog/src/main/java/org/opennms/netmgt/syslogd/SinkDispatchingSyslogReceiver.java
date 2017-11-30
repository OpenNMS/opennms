/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

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

import java.net.InetSocketAddress;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageDTO;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;

public class SyslogSinkModule extends AbstractXmlSinkModule<SyslogConnection, SyslogMessageLogDTO> {

    public static final String MODULE_ID = "Syslog";

    private final SyslogdConfig config;
    private final DistPollerDao distPollerDao;

    public SyslogSinkModule(SyslogdConfig config, DistPollerDao distPollerDao) {
        super(SyslogMessageLogDTO.class);
        this.config = Objects.requireNonNull(config);
        this.distPollerDao = Objects.requireNonNull(distPollerDao);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public int getNumConsumerThreads() {
        return config.getNumThreads();
    }

    @Override
    public AggregationPolicy<SyslogConnection, SyslogMessageLogDTO, SyslogMessageLogDTO> getAggregationPolicy() {
        final String systemId = distPollerDao.whoami().getId();
        final String systemLocation = distPollerDao.whoami().getLocation();
        return new AggregationPolicy<SyslogConnection, SyslogMessageLogDTO, SyslogMessageLogDTO>() {
            @Override
            public int getCompletionSize() {
                return config.getBatchSize();
            }

            @Override
            public int getCompletionIntervalMs() {
                return config.getBatchIntervalMs();
            }

            @Override
            public Object key(SyslogConnection syslogConnection) {
                return syslogConnection.getSource();
            }

            @Override
            public SyslogMessageLogDTO aggregate(SyslogMessageLogDTO accumulator, SyslogConnection connection) {
                if (accumulator == null) {
                    accumulator = new SyslogMessageLogDTO(systemLocation, systemId, connection.getSource());
                }
                SyslogMessageDTO messageDTO = new SyslogMessageDTO(connection.getBuffer());
                accumulator.getMessages().add(messageDTO);
                return accumulator;
            }

            @Override
            public SyslogMessageLogDTO build(SyslogMessageLogDTO accumulator) {
                return accumulator;
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return config.getQueueSize();
            }

            @Override
            public int getNumThreads() {
                return config.getNumThreads();
            }

            @Override
            public boolean isBlockWhenFull() {
                return true;
            }
        };
    }

    @Override
    public SyslogConnection unmarshalSingleMessage(byte[] bytes) {
        SyslogMessageLogDTO syslogMessageLogDTO = unmarshal(bytes);
        SyslogMessageDTO syslogMessageDTO = syslogMessageLogDTO.getMessages().get(0);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(syslogMessageLogDTO.getSourceAddress(), syslogMessageLogDTO.getSourcePort());
        return new SyslogConnection(inetSocketAddress, syslogMessageDTO.getBytes());
    }

    /**
     * Used for testing.
     */
    public SyslogMessageLogDTO toMessageLog(SyslogConnection... connections) {
        final String systemId = distPollerDao.whoami().getId();
        final String systemLocation = distPollerDao.whoami().getLocation();
        if (connections.length < 1) {
            throw new IllegalArgumentException("One or more connection are required.");
        }
        final SyslogMessageLogDTO messageLog = new SyslogMessageLogDTO(systemLocation, systemId,
                connections[0].getSource());
        for (SyslogConnection connection : connections) {
            final SyslogMessageDTO messageDTO = new SyslogMessageDTO(connection.getBuffer());
            messageLog.getMessages().add(messageDTO);
        }
        return messageLog;
    }

    @Override
    public int hashCode() {
        return Objects.hash(MODULE_ID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
}

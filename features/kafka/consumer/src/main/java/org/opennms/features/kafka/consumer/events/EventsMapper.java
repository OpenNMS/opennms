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
package org.opennms.features.kafka.consumer.events;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class EventsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(EventsMapper.class);

    public static Event toEvent(EventsProto.Event pbEvent) {
        if (Strings.isNullOrEmpty(pbEvent.getUei())) {
            LOG.warn("Event will not be forwarded, `uei` is required field, skipped Event : \n {}", pbEvent);
            return null;
        }
        if (Strings.isNullOrEmpty(pbEvent.getSource())) {
            LOG.warn("Event will not be forwarded, `source` is required field, skipped Event : \n {}", pbEvent);
            return null;
        }
        final EventBuilder builder = new EventBuilder(pbEvent.getUei(), pbEvent.getSource());
        builder.setSeverity(OnmsSeverity.get(pbEvent.getSeverity().name()).getLabel());
        getString(pbEvent.getHost()).ifPresent(builder::setHost);
        if (pbEvent.getNodeId() > 0) {
            builder.setNodeid(pbEvent.getNodeId());
        }
        getString(pbEvent.getIpAddress()).ifPresent(ip -> builder.setInterface(InetAddressUtils.getInetAddress(ip)));
        getString(pbEvent.getServiceName()).ifPresent(builder::setService);
        if (pbEvent.getIfIndex() > 0) {
            builder.setIfIndex(pbEvent.getIfIndex());
        }
        getString(pbEvent.getDistPoller()).ifPresent(builder::setDistPoller);
        getString(pbEvent.getDescription()).ifPresent(builder::setDescription);
        getString(pbEvent.getLogDest()).ifPresent(builder::setLogDest);
        getString(pbEvent.getLogContent()).ifPresent(builder::setLogMessage);
        for (EventsProto.EventParameter p : pbEvent.getParameterList()) {
            builder.setParam(p.getName(), p.getValue());
        }
        return builder.getEvent();
    }

    public static List<Event> mapProtobufToEvents(List<EventsProto.Event> pbEvents) {
        return pbEvents.stream().map(EventsMapper::toEvent).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static Optional<String> getString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}

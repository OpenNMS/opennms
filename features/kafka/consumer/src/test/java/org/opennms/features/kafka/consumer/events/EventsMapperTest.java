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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;

public class EventsMapperTest {

    @Test
    public void testEventWithoutRequiredFields() {

        List<EventsProto.Event> protobufEvents = new ArrayList<>();
        // Add an event without source and empty uei
        EventsProto.Event.Builder builder = EventsProto.Event.newBuilder();
        builder.setUei("");
        builder.setSource("");
        builder.setDescription("Event without uei/source");
        protobufEvents.add(builder.build());
        builder = EventsProto.Event.newBuilder();
        builder.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        builder.setSource("kafka-test");
        builder.setIpAddress("192.168.1.2");
        builder.setDescription("Kafka Consumer description");
        protobufEvents.add(builder.build());
        List<Event> events = EventsMapper.mapProtobufToEvents(protobufEvents);
        Assert.assertThat(events.size(), Matchers.is(1));
    }
}

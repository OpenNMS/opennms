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
package org.opennms.netmgt.config;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.xml.eventconf.Events;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataRetrievalFailureException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public final class EventConfUtil {

    private static final AtomicLong sourceIdGenerator = new AtomicLong();
    private static final AtomicLong eventIdGenerator = new AtomicLong();

    @VisibleForTesting
    // This method should only be used in tests.
    public static List<EventConfEvent> parseResourcesAsEventConfEvents(Resource configResource) throws IOException {
        List<EventConfEvent> eventConfEventList = new ArrayList<>();

        try {
            Events unmarshalEvents = JaxbUtils.unmarshal(Events.class, configResource);
            unmarshalEvents.loadEventFiles(configResource);
            unmarshalEvents.getLoadedEventFiles().put(configResource.getFilename(), unmarshalEvents);

            Map<String, Events> fileEventsMap = unmarshalEvents.getLoadedEventFiles();
            int fileOrder = 1;

            for (Map.Entry<String, Events> entry : fileEventsMap.entrySet()) {
                String fileName = entry.getKey() == null ? "" : entry.getKey();
                Events events = entry.getValue();

                String withoutExtension = fileName.endsWith(".xml")
                        ? fileName.substring(0, fileName.lastIndexOf(".xml"))
                        : fileName;

                EventConfSourceMetadataDto metadataDto = new EventConfSourceMetadataDto.Builder()
                        .filename(withoutExtension)
                        .now(new Date())
                        .vendor(StringUtils.substringBefore(fileName, "."))
                        .username("system")
                        .description("")
                        .eventCount(events.getEvents().size())
                        .fileOrder(fileOrder++)
                        .build();

                EventConfSource source = createSource(metadataDto);

                eventConfEventList.addAll(
                        getEventConfEventList(source, events, metadataDto.getUsername(), metadataDto.getNow())
                );
            }
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Unable to load " + configResource, e);
        }

        return eventConfEventList;
    }

    private static EventConfSource createSource(final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = new EventConfSource();
        source.setId(sourceIdGenerator.incrementAndGet());
        source.setCreatedTime(eventConfSourceMetadataDto.getNow());
        source.setName(eventConfSourceMetadataDto.getFilename());
        source.setFileOrder(eventConfSourceMetadataDto.getFileOrder());
        source.setEventCount(eventConfSourceMetadataDto.getEventCount());
        source.setEnabled(true);
        source.setUploadedBy(eventConfSourceMetadataDto.getUsername());
        source.setLastModified(eventConfSourceMetadataDto.getNow());
        source.setVendor(eventConfSourceMetadataDto.getVendor());
        source.setDescription(eventConfSourceMetadataDto.getDescription());
        return source;
    }

    private static List<EventConfEvent> getEventConfEventList(EventConfSource source, Events events, String username, Date now) {
        List<EventConfEvent> eventEntities = events.getEvents().stream().map(parsed -> {
            EventConfEvent event = new EventConfEvent();
            event.setId(eventIdGenerator.incrementAndGet());
            event.setSource(source);
            event.setUei(parsed.getUei());
            event.setEventLabel(parsed.getEventLabel());
            event.setDescription(parsed.getDescr());
            event.setEnabled(true);
            event.setXmlContent(JaxbUtils.marshal(parsed));
            event.setCreatedTime(now);
            event.setLastModified(now);
            event.setModifiedBy(username);
            return event;
        }).toList();
        return eventEntities;
    }

}

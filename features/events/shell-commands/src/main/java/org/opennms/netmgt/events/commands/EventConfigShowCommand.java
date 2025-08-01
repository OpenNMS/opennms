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
package org.opennms.netmgt.events.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.Event;

/**
 * View the eventconf from the Karaf shell.
 *
 * @author jwhite
 */
@Command(scope = "opennms", name = "show-event-config", description = "Renders the matched event definitions to XML. " +
        "This command makes it possible to view event definitions which are not serialized on disk.")
@Service
public class EventConfigShowCommand implements Action {

    @Reference
    public EventConfDao eventConfDao;

    @Option(name = "-l", aliases = "--limit", description = "Limit the number of event definitions that are shown.")
    int limit = 10;

    @Option(name = "-u", aliases = "--uei", description = "Event UEI substring to match.", required = true)
    @Completion(EventUeiCompleter.class)
    String eventUeiMatch;

    @Override
    public Object execute() {
        // Find all the event UEIs that contain the given substring
        final Set<String> matchedEventUeis = eventConfDao.getEventUEIs().stream()
                .filter(uei -> uei.toLowerCase().contains(eventUeiMatch.toLowerCase()))
                .sorted(Comparator.comparing(uei -> uei))
                .limit(limit)
                .collect(Collectors.toSet());

        // Retrieve the event definitions for the given UEIs
        final List<Event> matchedEvents = matchedEventUeis.stream()
                .flatMap(uei -> eventConfDao.getEvents(uei).stream())
                // The event *may* have disappeared since we matched it above, ignore these.
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Marshal to XML and print to stdout
        int count = 1;
        for (Event matched : matchedEvents) {
            String eventConfXml = JaxbUtils.marshal(matched);
            System.out.printf("Event #%d\n", count);
            System.out.println(eventConfXml);
            System.out.println();
            count++;
        }

        return null;
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
@Command(scope = "events", name = "show-event-config", description = "Renders the matched event definitions to XML. " +
        "This command makes it possible to view event definitions which are not seriliazed on disk.")
@Service
public class EventConfigShowCommand implements Action {

    @Reference
    public EventConfDao eventConfDao;

    @Option(name="-l", aliases="--limit", description="Limit the number of event definitions that are shown.")
    int limit = 10;

    @Option(name="-u", aliases="--uei", description="Event UEI substring to match.", required = true)
    @Completion(EventUeiCompleter.class)
    String eventUeiMatch;

    @Override
    public Object execute() {
        // Find all of the event UEIs that contain the given substring
        final List<String> matchedEventUeis = eventConfDao.getEventUEIs().stream()
                .filter(uei -> uei.toLowerCase().contains(eventUeiMatch.toLowerCase()))
                .sorted(Comparator.comparing(uei -> uei))
                .limit(limit)
                .collect(Collectors.toList());
        // Retrieve the event definitions for the given UEIs
        final List<Event> matchedEvents = matchedEventUeis.stream()
                .map(uei -> eventConfDao.findByUei(uei))
                // The event *may* have dissapeared since we matched it above, ignore these.
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

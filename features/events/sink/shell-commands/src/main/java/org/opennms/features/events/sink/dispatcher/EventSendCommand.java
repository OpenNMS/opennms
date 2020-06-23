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

package org.opennms.features.events.sink.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

@Command(scope = "events", name = "send", description = "Send event with specified uei and params")
@Service
public class EventSendCommand implements Action {

    @Reference
    private EventForwarder eventForwarder;

    @Option(name="-u", aliases="--uei", description="events uei", required=true, multiValued=false)
    String eventUei;

    @Argument(index = 0, name = "parameters", description = "Parameters in key=value form", multiValued = true)
    List<String> params;

    @Override
    public Object execute() throws Exception {

        if (eventUei == null) {
            System.out.println("Event uei need to specified with -u or --uei option");
        }
        EventBuilder eventBuilder = new EventBuilder(eventUei, "karaf-shell");
        // parse and add params
        Map<String, String> parameters = parse(params);
        parameters.forEach(eventBuilder::addParam);
        // send event
        eventForwarder.sendNow(eventBuilder.getEvent());
        System.out.printf("Event with uei '%s' is being sent asynchronously \n ", eventUei);
        return null;
    }

    private static Map<String, String> parse(List<String> params) {
        Map<String, String> properties = new HashMap<>();
        if (params != null) {
            for (String keyValue : params) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid param " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.net.UnknownHostException;
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

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "send-event", description = "Send event with specified uei and params")
@Service
public class EventSendCommand implements Action {

    @Reference
    private EventForwarder eventForwarder;
    
    @Option(name="-n", aliases="--nodeid", description="Database ID of associated node (or use parameters _foreignSource, _foreignId)", required=false, multiValued=false)
    private Long nodeId;
    
    @Option(name="-i", aliases="--interface", description="IP address of associated interface", required=false, multiValued=false)
    private String ipinterface;
    
    @Option(name="-s", aliases="--service", description="Name of the associated service", required=false, multiValued=false)
    private String service;
    
    @Option(name="-f", aliases="--ifindex", description="ifIndex of the associated L2 interface", required=false, multiValued=false)
    private Integer ifIndex;
    
    @Option(name="-x", aliases="--severity", description="Severity of the event (Indeterminate|Cleared|Normal|Warning|Minor|Major|Critical)", required=false, multiValued=false)
    private String severity;
    
    @Option(name="-d", aliases="--description", description="A description for the event browser", required=false, multiValued=false)
    private String descr;
    
    @Option(name="-l", aliases="--logmsg", description="A short logmsg for the event browser (secure field by default)", required=false, multiValued=false)
    private String logmsg;

    @Option(name = "-p", aliases="--parameter", description = "Parameter in key=value form", required = false, multiValued = true)
    List<String> params;

    @Argument(index = 0, name="uei", description="Event UEI", required=true, multiValued=false)
    private String eventUei;

    @Override
    public Object execute() {

        if (eventUei == null) {
            System.out.println("Event uei need to specified with -u or --uei option");
        }
        EventBuilder eventBuilder = new EventBuilder(eventUei, "KarafShell_send-event");
        
        if (nodeId != null) eventBuilder.setNodeid(nodeId);
        if (! Strings.isNullOrEmpty(ipinterface))
            try {
                eventBuilder.setInterface(InetAddress.getByName(ipinterface));
            } catch (UnknownHostException uhe) {
                System.out.println(String.format("Error: %s", uhe.getMessage()));
                return null;
            }
        if (! Strings.isNullOrEmpty(service)) eventBuilder.setService(service);
        if (ifIndex != null) eventBuilder.setIfIndex(ifIndex);
        if (! Strings.isNullOrEmpty(severity)) eventBuilder.setSeverity(canonicalizeSeverity(severity));
        if (! Strings.isNullOrEmpty(descr)) eventBuilder.setDescription(descr);
        if (! Strings.isNullOrEmpty(logmsg)) eventBuilder.setLogMessage(logmsg);
        
        // parse and add params
        Map<String, String> parameters = parseParams(params);
        parameters.forEach(eventBuilder::addParam);
        // send event
        eventForwarder.sendNow(eventBuilder.getEvent());
        System.out.printf("Event with uei '%s' is being sent asynchronously \n ", eventUei);
        return null;
    }

    private static Map<String, String> parseParams(List<String> params) {
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
    
    private String canonicalizeSeverity(String input) {
        if (Strings.isNullOrEmpty(input)) return "Indeterminate";
        switch(input.toLowerCase()) {
            case "indeterminate": return "Indeterminate";
            case "cleared": return "Cleared";
            case "normal": return "Normal";
            case "warning": return "Warning";
            case "minor": return "Minor";
            case "major": return "Major";
            case "critical": return "Critical";
        }
        return "Indeterminate";
    }

}

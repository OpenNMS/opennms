/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.eifadapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class EifParser {

    private static final Logger LOG = LoggerFactory.getLogger(EifParser.class);
    private static final int eifStartOffset = 37;

    enum EifSeverity {
        FATAL, CRITICAL, MINOR, WARNING, OK, INFO, HARMLESS, UNKNOWN;

        public OnmsSeverity toOnmsSeverity() {
            switch (this) {
                case UNKNOWN:
                    return OnmsSeverity.INDETERMINATE;
                case HARMLESS:
                case INFO:
                case OK:
                    return OnmsSeverity.NORMAL;
                case WARNING:
                    return OnmsSeverity.WARNING;
                case MINOR:
                    return OnmsSeverity.MINOR;
                case CRITICAL:
                    return OnmsSeverity.MAJOR;
                case FATAL:
                    return OnmsSeverity.CRITICAL;
                default:
                    throw new IllegalArgumentException("No mapping for " + name() + " found");
            }
        }
    }

    public static List<Event> translateEifToOpenNMS(NodeDao nodeDao, StringBuilder eifBuff) {

        // Create a list of events to return to the packet processor
        List<Event> translatedEvents = new ArrayList<>();

        // Loop over the received EIF package until we run out of events
        while(eifBuff.length() > 0 && eifBuff.indexOf(";END") > 1) {
            // Extract a single event from the package
            int eventStart = eifBuff.indexOf("<START>>");
            int eventEnd = eifBuff.indexOf(";END");
            String eifEvent = eifBuff.substring(eventStart + eifStartOffset,eventEnd);
            eifBuff.delete(0,eventEnd+4);

            // Parse the EIF slots into OpenNMS parms, and try to look up the source's nodeId
            String eifClass = eifEvent.split(";")[0];
            String eifSlots = eifEvent.substring(eifClass.length()+1,eifEvent.length()).
                    replaceAll(System.getProperty("line.separator"),"");
            Map<String, String> eifSlotMap = parseEifSlots(eifSlots);
            List<Parm> parmList = new ArrayList<>();
            eifSlotMap.entrySet().forEach(p -> parmList.add(new Parm(p.getKey(),p.getValue())));
            long nodeId = connectEifEventToNode(nodeDao, eifSlotMap);

            // Add the translated event to the list
            translatedEvents.add(
                    new EventBuilder("uei.opennms.org/vendor/IBM/EIF/"+eifClass,"eif").
                            setNodeid(nodeId).
                            setSeverity(EifSeverity.valueOf(eifSlotMap.get("severity")).toOnmsSeverity().getLabel()).
                            setParms(parmList).getEvent());

        }

        if(translatedEvents.isEmpty()) {
            LOG.error("Received a zero-length list");
            return null;
        }
        return translatedEvents;
    }

    public static Map<String, String> parseEifSlots(String eifBodyString) {

        Map<String, String> mappedEifSlots = new HashMap<>();
        List<String> slotArray = Arrays.asList(eifBodyString.split(";"));
        for ( int i = 0; i < slotArray.size(); i += 1) {
            slotArray.get(i).replaceAll("[ ']","");
            if (slotArray.get(i).length() == 0) { continue; }
            String[] slotKeyValue = slotArray.get(i).split("=");
            // If the array only has 1 element, a prior slot value was malformed. Skip this element.
            if ( slotKeyValue.length < 2 ) { continue; }
            mappedEifSlots.put(slotKeyValue[0], slotKeyValue[1].replaceAll("^\"|^'|\"$|'$", ""));
        }

        return mappedEifSlots;
    }

    private static long connectEifEventToNode(NodeDao nodeDao, Map<String, String> eifSlotMap) {
        /*
         * Available slots for identifying the node:
         * fqhostname - Fully qualified host name, if available.
         * hostname - Hostname of the managed system that originated the event, if available.
         * origin - TCP/IP address of the originating managed system in dotted-decimal notation, if available.
         */
        long nodeId = 0;
        String fqdn = "";
        if (!"".equals(eifSlotMap.get("fqhostname")) && eifSlotMap.get("fqhostname") != null) {
            fqdn = eifSlotMap.get("fqhostname");
        } else if (!"".equals(eifSlotMap.get("hostname")) && eifSlotMap.get("hostname") != null) {
            String hostname = eifSlotMap.get("hostname");
            try {
                fqdn = InetAddress.getByName(hostname).getCanonicalHostName();
            } catch (UnknownHostException uhe) {
                LOG.error("UnknownHostException while resolving hostname {}",hostname);
            }
        }
        // if a FQDN can't be found using fqhostname or hostname, fall back to the origin IP address
        if ("".equals(fqdn) && !"".equals(eifSlotMap.get("origin")) && eifSlotMap.get("origin") != null) {
            String origin = eifSlotMap.get("origin");
            if ( InetAddresses.isInetAddress(origin) ) {
                try {
                    fqdn = InetAddress.getByAddress(origin.getBytes()).getCanonicalHostName();
                } catch (UnknownHostException uhe) {
                    LOG.error("UnknownHostException while resolving origin {}", origin);
                    fqdn = origin;
                }
            }
        }

        // attempt to look up the nodeId using the FQDN
        if(!"".equals(fqdn) && !fqdn.equals(null)) {
            List<OnmsNode> matchingNodes = new ArrayList<>();
            OnmsNode firstMatch;
            try {
                matchingNodes = nodeDao.findByLabel(fqdn);
            } catch (NullPointerException npe) {
                LOG.debug("No node located for {}",fqdn);
            }
            if ( matchingNodes.size() <= 0 && !InetAddresses.isInetAddress(fqdn) ) {
                try {
                    matchingNodes = nodeDao.findByLabel(fqdn.split("\\.")[0]);
                } catch (NullPointerException npe) {
                    LOG.debug("No node located for {}",fqdn.split("\\.")[0]);
                }
            }

            firstMatch = ( matchingNodes.size() > 0 ? matchingNodes.get(0) : null );

            if(firstMatch != null) {
                nodeId = Long.valueOf(firstMatch.getNodeId());
            }
        }

        if(nodeId == 0) {
            LOG.debug("connectEifEventToNode : No matching nodes found. Defaulting to nodeId 0.");
        }

        return nodeId;
    }
}

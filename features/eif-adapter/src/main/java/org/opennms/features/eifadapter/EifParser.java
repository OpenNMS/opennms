/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.eifadapter;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EifParser {

    public static List<Event> translateEifToOpenNMS(StringBuilder eifBuff) {

        // Create a list of events to return to the packet processor
        List<Event> translatedEvents = new ArrayList<>();
        // Loop over the received EIF package until we run out of events
        while(eifBuff.length() > 0 && eifBuff.indexOf(";END") > 1) {
            int eventStart = eifBuff.indexOf("<START>>");
            int eventEnd = eifBuff.indexOf(";END");
            String eifEvent =  eifBuff.substring(eventStart+8,eventEnd);
            eifBuff.delete(0,eventEnd+4);
            Pattern eifClassPattern = Pattern.compile("^.*?(\\w{2,}+);.*");
            Matcher eifClassMatcher = eifClassPattern.matcher(eifEvent);
            if (eifClassMatcher.matches()) {
                String eifClass = eifClassMatcher.group(1);
                // Find the end of the eifClass string, so we can parse slots from the rest of the message body
                int eifClassEnd = eifEvent.toString().indexOf(eifClassMatcher.group(1))+eifClassMatcher.group(1).
                        length();
                // Remove newlines from the event body
                String eifSlots = eifEvent.substring(1+eifClassEnd,eifEvent.length()).
                        replaceAll(System.getProperty("line.separator"),"");
                // Parse the EIF slots into OpenNMS parms
                List<Parm> parmList = new ArrayList<>();
                parseEifSlots(eifSlots).entrySet().forEach(p -> parmList.add(new Parm(p.getKey(),p.getValue())));
                // Add the translated event to the list
                translatedEvents.add(new EventBuilder("org.opennms.eif/"+eifClass,"eif").setParms(parmList).getEvent());
            } else {
                System.err.println("EIF class match failed");
            }
        }
        if(translatedEvents.size() > 0) {
            return translatedEvents;
        } else {
            System.err.println("Received a zero-length list");
            return null;
        }
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
}

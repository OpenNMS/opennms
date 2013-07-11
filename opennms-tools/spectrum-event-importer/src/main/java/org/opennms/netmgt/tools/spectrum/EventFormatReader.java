/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;

public class EventFormatReader {
    private Resource m_resource;
    private BufferedReader m_reader;
    
    /**
     *
     * {d "%w- %d %m-, %Y - %T"} - A "bwNetworkRoutingServiceRouteExhaustion" event has occurred, from {t} device, named {m}.
     *
     * "For the actual description, refer the BroadWorks FaultManagementGuide as it may contain variable data."
     *
     * identifier = {I 1}
     * timeStamp = {S 2}
     * alarmName = {S 3}
     * systemName = {S 4}
     * severity = {T severity 5}
     * component = {T component 6}
     * subcomponent = {T subcomponent 7}
     * problemText = {S 8}
     * recommendedActionsText = {S 9}
     * (event [{e}])
     * 
     */
    
    public EventFormatReader(Resource rsrc) throws IOException {
        m_resource = rsrc;
        m_reader = new BufferedReader(new InputStreamReader(m_resource.getInputStream()));
    }
    
    public EventFormat getEventFormat() throws IOException {
        String fileName = m_resource.getFilename();
        String eventCode = "deadbeef";
        Matcher m = Pattern.compile("^Event([0-9A-Fa-f]+)$").matcher(fileName);
        if (m.matches()) {
            eventCode = "0x" + m.group(1);
        }
        
        EventFormat ef = new EventFormat(eventCode);
        StringBuilder contents = new StringBuilder("");
        String thisLine;
        
        while ((thisLine = m_reader.readLine()) != null) {
            contents.append(thisLine).append("\n");
        }
        
        ef.setContents(contents.toString());
        return ef;
    }    
}

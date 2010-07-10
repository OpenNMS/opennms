/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 9, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
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

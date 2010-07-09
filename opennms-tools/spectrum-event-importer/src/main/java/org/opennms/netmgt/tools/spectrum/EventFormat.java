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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventFormat {
    private String m_eventCode;
    private String m_contents;
    
    public EventFormat(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event-code parameter must not be null");
        }
        m_eventCode = eventCode;
    }

    public String getEventCode() {
        return m_eventCode;
    }

    public void setEventCode(String eventCode) {
        if (eventCode == null) {
            throw new IllegalArgumentException("The event-code must not be null");
        }
        m_eventCode = eventCode;
    }
    
    public String getContents() {
        return m_contents;
    }
    
    public void setContents(String contents) {
        if (contents == null) {
            throw new IllegalArgumentException("The contents must not be null");
        }
        m_contents = contents;
    }
    
    public List<String> getSubstTokens() {
        List<String> tokens = new ArrayList<String>();
        
        Matcher m = Pattern.compile("(?s)(\\{.*?\\})").matcher(m_contents);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        
        return tokens;
    }

}
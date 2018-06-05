/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
        List<String> tokens = new ArrayList<>();
        
        Matcher m = Pattern.compile("(?s)(\\{.*?\\})").matcher(m_contents);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        
        return tokens;
    }

}
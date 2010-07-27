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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package com.brozowski.instrumentation;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import edu.ncsu.pdgrenon.BaseLogMessage;

/**
 * Collector
 *
 * @author brozow
 */
public class Collector {
    
    List<BaseLogMessage> m_messages = new LinkedList<BaseLogMessage>();

    public Date getStartTime() {
        return m_messages.get(0).getDate();
    }
    
    public Date getEndTime() {
        return m_messages.get(m_messages.size()-1).getDate();
    }
    
    public Duration getDuration() {
        return new Duration(getStartTime(), getEndTime());
    }

    public void addLog(String logString) {
        BaseLogMessage msg = BaseLogMessage.create(logString);
        if (msg != null) {
            m_messages.add(msg);
        }
        
    }

}

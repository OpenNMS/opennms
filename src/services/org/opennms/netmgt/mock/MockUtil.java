//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MockUtil {

    private static boolean s_loggingSetup = false;

    public static void addEventParm(Event event, String parmName, int parmValue) {
        addEventParm(event, parmName, String.valueOf(parmValue));
    }

    public static void addEventParm(Event event, String parmName, String parmValue) {
        Parms parms = event.getParms();
        if (parms == null) {
            parms = new Parms();
            event.setParms(parms);
        }
        Parm parm = new Parm();
        parm.setParmName(parmName);
        Value value = new Value();
        value.setContent(parmValue);
        parm.setValue(value);
        parms.addParm(parm);
    }

    public static Event createEvent(String uei, int nodeId, String ipAddr, String svcName) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId);
        event.setInterface(ipAddr);
        event.setService(svcName);
        return event;
    }

    /**
     * @param string
     * @param i
     * @param j
     * @return
     */
    public static Event createReparentEvent(String ipAddr, int oldNode, int newNode) {
        Event event = new Event();
        event.setUei(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);
        event.setNodeid(oldNode);
        event.setInterface(ipAddr);
        addEventParm(event, EventConstants.PARM_OLD_NODEID, oldNode);
        addEventParm(event, EventConstants.PARM_NEW_NODEID, newNode);
        return event;
    }

    /**
     * @param expectedEvent
     * @param receivedEvent
     * @return
     */
    public static boolean eventsMatch(Event e1, Event e2) {
        if (e1 == e2)
            return true;
        if (e1 == null || e2 == null)
            return false;

        return (e1.getUei() == e2.getUei() || e1.getUei().equals(e2.getUei())) && (e1.getNodeid() == e2.getNodeid()) && (e1.getInterface() == e2.getInterface() || e1.getInterface().equals(e2.getInterface())) && (e1.getService() == e2.getService() || e1.getService().equals(e2.getService()));
    }

    /**
     * 
     */
    public static void logToConsole() {
        if (!s_loggingSetup) {
            Properties logConfig = new Properties();

            logConfig.put("log4j.rootCategory", "DEBUG, POLLER");
            logConfig.put("log4j.appender.POLLER", "org.apache.log4j.ConsoleAppender");
            logConfig.put("log4j.appender.POLLER.layout", "org.apache.log4j.PatternLayout");
            logConfig.put("log4j.appender.POLLER.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
            PropertyConfigurator.configure(logConfig);
        }
    }

    public static void printEvent(String prefix, Event event) {
        if (prefix == null)
            prefix = "Event";
        System.err.println(prefix + ": " + event.getUei() + "/" + event.getNodeid() + "/" + event.getInterface() + "/" + event.getService());
    }

    public static void printEvents(String prefix, Collection events) {
        Iterator it = events.iterator();
        while (it.hasNext()) {
            printEvent(prefix, (Event) it.next());
        }
    }

}

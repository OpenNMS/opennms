//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
package org.opennms.netmgt.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>XmlrpcUtil class.</p>
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @version $Id: $
 */
public final class XmlrpcUtil {
    /**
     * This method is responsible for generating an xmlrpcNotification event and
     * sending it to eventd..
     *
     * @param txNo
     *            the transaction no.
     * @param sourceUei
     *            The uei of the source event that this event to report for.
     * @param message
     *            The message for external xmlrpc server.
     * @param status
     *            flag to indicate the type of this notification.
     * @param generator
     *            openNMS daemon where this event is produced.
     */
    public static void createAndSendXmlrpcNotificationEvent(final long txNo, final String sourceUei, final String message, final int status, final String generator) {
        ThreadCategory log = ThreadCategory.getInstance("XmlrpcUtil");
        if (log.isDebugEnabled())
            log.debug("createAndSendXmlrpcNotificationEvent:  txNo= " + txNo + "\n" + " uei = " + sourceUei + "\n" + " message = " + message + "\n" + " status = " + status);

        Event newEvent = new Event();
        newEvent.setUei(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI);
        newEvent.setSource(generator);

        String hostAddress = null;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhE) {
            hostAddress = "localhost";
            log.warn("createAndSendXmlrpcNotificationEvent: Could not lookup the host name for " + " the local host machine, address set to localhost", uhE);
        }

        newEvent.setHost(hostAddress);
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(txNo));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add source event uei
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_SOURCE_EVENT_UEI);
        parmValue = new Value();
        parmValue.setContent(new String(sourceUei));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add message parameter
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_SOURCE_EVENT_MESSAGE);
        parmValue = new Value();
        parmValue.setContent(message);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add status parameter
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_SOURCE_EVENT_STATUS);
        parmValue = new Value();
        parmValue.setContent(String.valueOf(status));
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        newEvent.setParms(eventParms);

        // Send event to Eventd
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            if (log.isDebugEnabled())
                log.debug("createdAndSendXmlrpcNotificationEvent: successfully sent " + "XMLRPC notification event for txno: " + txNo + " / " + sourceUei + " " + status);
        } catch (Throwable t) {
            log.warn("run: unexpected throwable exception caught during send to middleware", t);

            int failureFlag = 2;
            eventParms.removeParm(eventParm);

            // Add status parameter
            eventParm = new Parm();
            eventParm.setParmName(EventConstants.PARM_SOURCE_EVENT_STATUS);
            parmValue = new Value();
            parmValue.setContent(String.valueOf(failureFlag));
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);

            // Add Parms to the event
            newEvent.setParms(eventParms);
            try {
                EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

                if (log.isDebugEnabled())
                    log.debug("createdAndSendXmlrpcNotificationEvent: successfully sent " + "XMLRPC notification event for txno: " + txNo + " / " + sourceUei + " " + failureFlag);
            } catch (Throwable te) {
                log.warn("run: unexpected throwable exception caught during send to middleware", te);
            }
        }
    }
}

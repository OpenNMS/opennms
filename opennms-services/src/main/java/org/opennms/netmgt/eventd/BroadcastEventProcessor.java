// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;

public class BroadcastEventProcessor implements EventListener {
       
        Category m_log;
        
        BroadcastEventProcessor(EventIpcManager manager) {
           // Create the jms message selector
           installMessageSelector(manager);
           m_log = ThreadCategory.getInstance(getClass());
        }

       /**
        * Create message selector to set to the subscription
        */
       private void installMessageSelector(EventIpcManager manager) {
           // Create the JMS selector for the ueis this service is interested in
           //
           List ueiList = new ArrayList();

           // events config changed 
           ueiList.add(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI);
 
           manager.addEventListener(this, ueiList);
       }

       /**
        * </p>
        * Closes the current connections to the Java Message Queue if they are
        * still active. This call may be invoked more than once safely and may be
        * invoked during object finalization.
        * </p>
        * 
        */
       synchronized void close() {
           EventIpcManagerFactory.getIpcManager().removeEventListener(this);
       }

       /**
        * This method may be invoked by the garbage thresholding. Once invoked it
        * ensures that the <code>close</code> method is called <em>at least</em>
        * once during the cycle of this object.
        * 
        */
       protected void finalize() throws Throwable {
           close(); // ensure it's closed
       }

       public String getName() {
           return "Eventd:BroadcastEventProcessor";
       }

       /**
        * This method is invoked by the JMS topic session when a new event is
        * available for processing. Currently only text based messages are
        * processed by this callback. Each message is examined for its Universal
        * Event Identifier and the appropriate action is taking based on each UEI.
        * 
        * @param event
        *            The event message.
        * 
        */
       public void onEvent(Event event) {
           Category log = ThreadCategory.getInstance(getClass());

           // print out the uei
           //
           if (log.isDebugEnabled()) {
               log.debug("received event, uei = " + event.getUei());
           }
           if(event.getUei().equals(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI)) {
               try {
                   EventConfigurationManager.reload();
               } catch (Exception e) {
                   m_log.error("Could not reload events config because "+e.getMessage(), e);
               }
           }       
       } // end onEvent()

} // end class


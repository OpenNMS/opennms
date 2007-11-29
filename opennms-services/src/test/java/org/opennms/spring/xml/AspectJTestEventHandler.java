//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Moved plugin management and database synchronization out
//              of CapsdConfigFactory, use RrdTestUtils to setup RRD
//              subsystem, and move configuration files out of embedded
//              strings into src/test/resources. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8
package org.opennms.spring.xml;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.annotations.EventHandler;
import org.opennms.netmgt.utils.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;

@EventListener(name="AspectJTestEventHandler")
public class AspectJTestEventHandler {

    
    private Throwable thrownException = null;
    private int handlerCallCount = 0;
    
    public void setThrownException(Throwable throwable) {
        this.thrownException = throwable;
    }

    public int getHandlerCallCount() {
        return handlerCallCount;
    }

    public void setHandlerCallCount(int handlerCallCount) {
        this.handlerCallCount = handlerCallCount;
    }

    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleAnEvent(Event e) throws Throwable {
        System.err.println("Received Event "+e.getUei());
        handlerCallCount++;
        if (thrownException != null) {
            throw thrownException;
        }
    }

    public void reset() {
        handlerCallCount = 0;
        thrownException = null;
    }
}

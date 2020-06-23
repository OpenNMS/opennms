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

package org.opennms.spring.xml;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
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

    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
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

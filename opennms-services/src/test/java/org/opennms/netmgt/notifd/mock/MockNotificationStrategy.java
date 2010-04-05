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
// 2007 Apr 13: Genericize List passed to send method. - dj@opennms.org
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

package org.opennms.netmgt.notifd.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.Argument;
import org.opennms.netmgt.notifd.NotificationStrategy;
import org.opennms.test.mock.MockUtil;
/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MockNotificationStrategy implements NotificationStrategy {
    
    private static NotificationAnticipator s_anticpator = null;
    
    Map notificationsSent = new HashMap();
    
    static {
        MockUtil.println("Static initializer on "+ MockNotificationStrategy.class.getName());
    }
    
    public MockNotificationStrategy(){
        MockUtil.println("Created a "+ MockNotificationStrategy.class.getName());        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    public int send(List<Argument> arguments) {
        MockUtil.println("Message sent with arguments:"+arguments);
        
        MockNotification notification = new MockNotification();
        Iterator<Argument> it = arguments.iterator();
        while (it.hasNext()) {
            Argument arg = it.next();
            if (arg.getSwitch().equals("-subject")) {
                notification.setSubject(arg.getValue());
            } else if (arg.getSwitch().equals("-email")) {
                notification.setEmail(arg.getValue());
            } else if (arg.getSwitch().equals("-tm")) {
				notification.setTextMsg(arg.getValue());
            }
        }
        notification.setExpectedTime(System.currentTimeMillis());

        NotificationAnticipator anticipator = getAnticpator();
        
        if (anticipator != null) {
            anticipator.notificationReceived(notification);
        } else {
            throw new NullPointerException("anticipator is null");
        }

        return 0;
        
    }

    public static NotificationAnticipator getAnticpator() {
        return s_anticpator;
    }

    public static void setAnticipator(NotificationAnticipator anticpator) {
        s_anticpator = anticpator;
    }
}

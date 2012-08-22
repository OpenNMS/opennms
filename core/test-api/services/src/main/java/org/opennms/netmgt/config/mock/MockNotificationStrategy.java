/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.mock;

import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.mock.MockNotification;
import org.opennms.netmgt.mock.NotificationAnticipator;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.test.mock.MockUtil;
/**
 * @author david
 */
public class MockNotificationStrategy implements NotificationStrategy {
    
    private static NotificationAnticipator s_anticipator = null;
    
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
            if (arg.getSwitch().equals(NotificationManager.PARAM_SUBJECT)) {
                notification.setSubject(arg.getValue());
            } else if (arg.getSwitch().equals(NotificationManager.PARAM_EMAIL)) {
                notification.setEmail(arg.getValue());
            } else if (arg.getSwitch().equals(NotificationManager.PARAM_TEXT_MSG)) {
				notification.setTextMsg(arg.getValue());
            }
        }
        notification.setExpectedTime(System.currentTimeMillis());

        NotificationAnticipator anticipator = getAnticipator();
        
        if (anticipator != null) {
            anticipator.notificationReceived(notification);
        } else {
            throw new NullPointerException("anticipator is null");
        }

        return 0;
        
    }

    public static NotificationAnticipator getAnticipator() {
        return s_anticipator;
    }

    public static void setAnticipator(NotificationAnticipator anticipator) {
        s_anticipator = anticipator;
    }
}

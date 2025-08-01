/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.mock;

import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.mock.MockNotification;
import org.opennms.netmgt.mock.NotificationAnticipator;
import org.opennms.netmgt.model.notifd.Argument;
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
    @Override
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

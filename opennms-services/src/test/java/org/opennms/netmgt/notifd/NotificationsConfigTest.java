/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.mock.MockNotifdConfigManager;
import org.opennms.netmgt.config.mock.MockNotificationManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.test.mock.MockUtil;

public class NotificationsConfigTest {
    
    @Test
    public void testFormattedNotificationsXml() throws Exception {
        MockUtil.println("################# Running Test ################");
        MockLogAppender.setupLogging();
        
        MockNotifdConfigManager notifdConfig = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));

        NotificationManager manager = new MockNotificationManager(notifdConfig, null, ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifications-config-test.xml"));
        Notification n = manager.getNotification("crazyTestNotification");
        assertTrue(n.getTextMessage().contains("\n"));
    }
}

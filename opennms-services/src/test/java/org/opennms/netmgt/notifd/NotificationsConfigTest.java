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

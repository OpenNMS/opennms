package org.opennms.netmgt.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.notifd.NotificationsTestCase;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.netmgt.notifd.mock.MockNotificationManager;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

public class NotificationsConfigTest extends NotificationsTestCase {

    @Test
    public void testFormattedNotificationsXml() throws Exception {
        MockUtil.println("################# Running Test ################");
        MockLogAppender.setupLogging();
        
        MockNetwork network = createMockNetwork();
        MockDatabase db = createDatabase(network);
        MockNotifdConfigManager notifdConfig = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));

        NotificationManager manager = new MockNotificationManager(notifdConfig, db, ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifications.xml"));
        Notification n = manager.getNotification("crazyTestNotification");
        assertTrue(n.getTextMessage().contains("\n"));
    }


}

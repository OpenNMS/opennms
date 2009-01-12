package org.opennms.netmgt.notifd;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class XMPPNotificationTest {
    private XMPPNotificationManager m_xmppManager;

    @Before
    public void setUp() {
        System.setProperty("useSystemXMPPConfig", "true");
        System.setProperty("xmpp.server", "jabber.example.com");
        System.setProperty("xmpp.port", "5222");
        System.setProperty("xmpp.TLSEnabled", "true");
        System.setProperty("xmpp.selfSignedCertificateEnabled", "true");
        System.setProperty("xmpp.user", "test");
        System.setProperty("xmpp.pass", "testpass");
        m_xmppManager = XMPPNotificationManager.getInstance();
    }

    @Test
    @Ignore("requires a working test jabber server")
    public void testNotification() {
        m_xmppManager.sendMessage("test@jabber.example.com", "This is a single-user test.");
    }
    
    @Test
    @Ignore("requires a working test jabber server")
    public void testGroupNotification() {
        m_xmppManager.sendGroupChat("test@conference.jabber.example.com", "This is a conference test.");
    }
}

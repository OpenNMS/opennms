package org.opennms.netmgt.notifd;

import java.util.Collections;
import java.util.Date;

import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.xml.event.Event;

public class BroadcastEventProcessorTest extends NotificationsTestCase {

    private BroadcastEventProcessor m_processor;
    
    protected void setUp() throws Exception {
    	super.setUp();
    	m_processor = new BroadcastEventProcessor();
        m_processor.initExpandRe();
    }

    /**
     * Test calling expandNotifParms to see if the regular expression in
     * m_notifdExpandRE is initialized from NOTIFD_EXPANSION_PARM.
     */
    @SuppressWarnings("unchecked")
    public void testExpandNotifParms() {
        m_processor.expandNotifParms("%foo%", Collections.EMPTY_MAP);
    }

    /**
     * Trip a notification and see if the %noticeid% token gets expanded to a numeric
     * value in the subject and text message
     * 
     * @author Jeff Gehlbach <jeffg@jeffg.org>
     */
     public void testExpandNoticeId_Bug1745() throws Exception {
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        Event event = MockEventUtil.createServiceEvent("Test", "uei.opennms.org/test/noticeIdExpansion", svc, null);

        // We need to know what noticeID to expect -- whatever the NotificationManager
        // gives us, the next notice to come out will have noticeID n+1.  This isn't
        // foolproof, but it should work within the confines of JUnit as long as all
        // previous cases have torn down the mock Notifd.
        String antNID = Integer.toString(m_notificationManager.getNoticeId() + 1);

        Date testDate = new Date();
        long finishedNotifs = anticipateNotificationsForGroup("notification '" + antNID + "'", "Notification '" + antNID + "'", "InitialGroup", testDate, 0);
        MockEventUtil.setEventTime(event, testDate);

        m_eventMgr.sendEventToListeners(event);

        verifyAnticipated(finishedNotifs, 500);
     }
}

package org.opennms.netmgt.dao;

import java.util.Date;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationDaoTest extends AbstractDaoTestCase {

    public void setUp() throws Exception {
        //setPopulate(false);
        super.setUp();
    }
    
    public void testSave() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getDistPollerDao().load("localhost"));
        event.setEventCreateTime(new Date());
        event.setEventDescr("event dao test");
        event.setEventHost("localhost");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        event.setEventLogGroup("event dao test log group");
        event.setEventLogMsg("event dao test log msg");
        event.setEventSeverity(new Integer(7));
        event.setEventSource("EventDaoTest");
        event.setEventTime(new Date());
        event.setEventUei("uei://org/opennms/test/NotificationDaoTest");
        OnmsNode node = (OnmsNode) getNodeDao().findAll().iterator().next();
        OnmsIpInterface iface = (OnmsIpInterface)node.getIpInterfaces().iterator().next();
        OnmsMonitoredService service = (OnmsMonitoredService)iface.getMonitoredServices().iterator().next();
        event.setNode(node);
	    event.setServiceType(service.getServiceType());
        event.setIpAddr(iface.getIpAddress());
        OnmsNotification notification = new OnmsNotification();
        notification.setEvent(event);
        notification.setTextMsg("Tests are fun!");
        getNotificationDao().save(notification);
       
        OnmsNotification newNotification = getNotificationDao().load(notification.getNotifyId());
        assertEquals("uei://org/opennms/test/NotificationDaoTest", newNotification.getEvent().getEventUei());
        
        OnmsUserNotification userNotif = new OnmsUserNotification();
        userNotif.setNotification(notification);
        userNotif.setNotifyTime(new Date());
        userNotif.setUserId("OpenNMS User");
        userNotif.setMedia("E-mail");
        userNotif.setContactInfo("test@opennms.org");
        getUserNotificationDao().save(userNotif);
        
        assertNotNull(userNotif.getNotification());
        assertEquals(userNotif.getUserId(), "OpenNMS User");
//        assertNotNull(newEvent.getServiceType());
//        assertEquals(service.getNodeId(), newEvent.getNode().getId());
//        assertEquals(event.getIpAddr(), newEvent.getIpAddr());
    }
}

package org.opennms.netmgt.eventd.nms16978;

import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;

public class EventSinkConsumerIT extends org.opennms.netmgt.eventd.EventSinkConsumerIT {
    @Override
    public void setUp() throws Exception{
        MockEventIpcManager.setEventIdStart(10000000000L);
        super.setUp();
    }
}

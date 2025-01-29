package org.opennms.netmgt.eventd.nms16978;

import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;

public class EventdIT extends org.opennms.netmgt.eventd.EventdIT {
    @Override
    public void setUp() {
        ((EventDaoHibernate) getDatabasePopulator().getEventDao())
                .getHibernateTemplate()
                .getSessionFactory()
                .openSession()
                .createSQLQuery("ALTER SEQUENCE eventsNxtId RESTART WITH 10000000000")
                .executeUpdate();
        super.setUp();
    }
}

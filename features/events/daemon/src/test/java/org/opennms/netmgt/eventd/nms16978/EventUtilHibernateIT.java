package org.opennms.netmgt.eventd.nms16978;

import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;

public class EventUtilHibernateIT extends org.opennms.netmgt.eventd.EventUtilHibernateIT {
    @Override
    public void setUp() throws Exception{
        ((EventDaoHibernate) getDatabasePopulator().getEventDao())
                .getHibernateTemplate()
                .getSessionFactory()
                .openSession()
                .createSQLQuery("ALTER SEQUENCE eventsNxtId RESTART WITH 10000000000")
                .executeUpdate();
        super.setUp();
    }
}

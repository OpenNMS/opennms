package org.opennms.netmgt.eventd.nms16978;

import org.junit.Before;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;

public class HibernateEventWriterIT extends org.opennms.netmgt.eventd.processor.HibernateEventWriterIT {
    @Before
    public void setUp() throws Exception{
        ((EventDaoHibernate)getHibernateEventWriter().getEventDao())
                .getHibernateTemplate()
                .getSessionFactory()
                .openSession()
                .createSQLQuery("ALTER SEQUENCE eventsNxtId RESTART WITH 10000000000")
                .executeUpdate();
    }
}

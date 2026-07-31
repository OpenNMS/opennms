package org.opennms.netmgt.eventd.nms16978;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;

public class HibernateEventWriterIT extends org.opennms.netmgt.eventd.processor.HibernateEventWriterIT {
    @Before
    public void setUp() throws Exception{
        Session session = ((EventDaoHibernate)getHibernateEventWriter().getEventDao())
                .getHibernateTemplate()
                .getSessionFactory()
                .openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createNativeQuery("ALTER SEQUENCE eventsNxtId RESTART WITH 10000000000")
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}

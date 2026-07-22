package org.opennms.netmgt.eventd.nms16978;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;

public class EventdIT extends org.opennms.netmgt.eventd.EventdIT {
    @Override
    public void setUp() {
        Session session = ((EventDaoHibernate) getDatabasePopulator().getEventDao())
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
        super.setUp();
    }
}

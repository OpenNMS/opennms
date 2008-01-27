package org.opennms.netmgt.eventd;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventdConfigFactory;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.ThrowableAnticipator;

public class EventIpcManagerDefaultImplTest extends TestCase {
    @Override
    public void setUp() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();

        EventdConfigFactory.init();
    }
    
    public void testInitWithNoEventdConfigMgr() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("eventdConfigMgr not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInitWithNoEventExpander() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("eventExpander not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventdConfigMgr(EventdConfigFactory.getInstance());

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInitWithNoDataSource() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("dataSource not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventdConfigMgr(EventdConfigFactory.getInstance());
        EventExpander eventExpander = new EventExpander();
        eventExpander.setEventConfDao(EasyMock.createMock(EventConfDao.class));
        eventExpander.afterPropertiesSet();
        manager.setEventExpander(eventExpander);

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInitWithNoEventdServiceManager() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("eventdServiceManager not set"));

        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventdConfigMgr(EventdConfigFactory.getInstance());
        EventExpander eventExpander = new EventExpander();
        eventExpander.setEventConfDao(EasyMock.createMock(EventConfDao.class));
        eventExpander.afterPropertiesSet();
        manager.setDataSource(EasyMock.createMock(DataSource.class));
        manager.setEventExpander(eventExpander);

        try {
            manager.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testInit() throws Exception {
        EventIpcManagerDefaultImpl manager = new EventIpcManagerDefaultImpl();
        manager.setEventdConfigMgr(EventdConfigFactory.getInstance());
        EventExpander eventExpander = new EventExpander();
        eventExpander.setEventConfDao(EasyMock.createMock(EventConfDao.class));
        eventExpander.afterPropertiesSet();
        manager.setEventExpander(eventExpander);
        manager.setDataSource(EasyMock.createMock(DataSource.class));
        manager.setEventdServiceManager(EasyMock.createMock(EventdServiceManager.class));
        manager.afterPropertiesSet();
    }
}

package org.opennms.netmgt.dao.hibernate;

import java.io.FileNotFoundException;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

public class LocationMonitorDaoHibernateTest extends
        AbstractTransactionalDaoTestCase {
    private LocationMonitorDaoHibernate m_locationMonitorDao;
   
    
    @Override
    protected void onSetUpInTransactionIfEnabled() {
        m_locationMonitorDao = new LocationMonitorDaoHibernate();
    }
    
    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }
    
    public void testSetConfigResourceProduction() throws FileNotFoundException {
        m_locationMonitorDao.setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
    }
    
    public void testSetConfigResourceExample() throws FileNotFoundException {
        m_locationMonitorDao.setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("examples/monitoring-locations.xml")));
    }
    
    public void testSetConfigResourceNoLocations() throws FileNotFoundException {
        m_locationMonitorDao.setMonitoringLocationConfigResource(new FileSystemResource("src/test/resources/monitoring-locations-no-locations.xml"));
    }

    
    public void testBogusConfig() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new CastorDataAccessFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.setMonitoringLocationConfigResource(new FileSystemResource("some bogus filename"));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindAllLocationDefinitionsPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.findAllLocationDefinitions();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindAllMonitoringLocationDefinitionsPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.findAllMonitoringLocationDefinitions();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.findMonitoringLocationDefinition("test");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionNull() throws FileNotFoundException {
        m_locationMonitorDao.setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.findMonitoringLocationDefinition(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionBogus() throws FileNotFoundException {
        m_locationMonitorDao.setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        assertNull("should not have found monitoring location definition--"
                   + "should have returned null",
                   m_locationMonitorDao.findMonitoringLocationDefinition("bogus"));
    }
}

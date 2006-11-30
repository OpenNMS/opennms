package org.opennms.netmgt.dao.hibernate;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

public class LocationMonitorDaoHibernateTest extends
        AbstractTransactionalDaoTestCase {
	
	public LocationMonitorDaoHibernateTest() {
		setPopulate(false);
	}

    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }
    
    public void testSaveLocationMonitor() {
    	Map <String, String> pollerDetails = new HashMap<String, String>();
    	pollerDetails.put("os.name", "BogOS");
    	pollerDetails.put("os.version", "sqrt(-1)");
    	
    	OnmsLocationMonitor mon = new OnmsLocationMonitor();
    	mon.setStatus(MonitorStatus.STARTED);
    	mon.setLastCheckInTime(new Date());
    	mon.setDetails(pollerDetails);
    	mon.setDefinitionName("RDU");
    	
    	getLocationMonitorDao().save(mon);
    	
    	getLocationMonitorDao().flush();
    	getLocationMonitorDao().clear();
		Object[] args = { mon.getId() };
    	
    	assertEquals(2, getJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", args));
    	
    	OnmsLocationMonitor mon2 = getLocationMonitorDao().get(mon.getId());
    	assertNotSame(mon, mon2);
    	assertEquals(mon.getStatus(), mon2.getStatus());
    	assertEquals(mon.getLastCheckInTime(), mon2.getLastCheckInTime());
    	assertEquals(mon.getDefinitionName(), mon2.getDefinitionName());
    	assertEquals(mon.getDetails(), mon2.getDetails());
    }
    
    
    
    public void testSetConfigResourceProduction() throws FileNotFoundException {
        getLocationMonitorDao().setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
    }
    
    public void testSetConfigResourceExample() throws FileNotFoundException {
        getLocationMonitorDao().setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("examples/monitoring-locations.xml")));
    }
    
    public void testSetConfigResourceNoLocations() throws FileNotFoundException {
        getLocationMonitorDao().setMonitoringLocationConfigResource(new FileSystemResource("src/test/resources/monitoring-locations-no-locations.xml"));
    }

    
    public void testBogusConfig() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new CastorDataAccessFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            getLocationMonitorDao().setMonitoringLocationConfigResource(new FileSystemResource("some bogus filename"));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindAllLocationDefinitionsPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	new LocationMonitorDaoHibernate().findAllLocationDefinitions();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindAllMonitoringLocationDefinitionsPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	new LocationMonitorDaoHibernate().findAllMonitoringLocationDefinitions();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionPropsNotSet() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	new LocationMonitorDaoHibernate().findMonitoringLocationDefinition("test");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionNull() throws FileNotFoundException {
        getLocationMonitorDao().setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            getLocationMonitorDao().findMonitoringLocationDefinition(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindMonitoringLocationDefinitionBogus() throws FileNotFoundException {
        getLocationMonitorDao().setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        assertNull("should not have found monitoring location definition--"
                   + "should have returned null",
                   getLocationMonitorDao().findMonitoringLocationDefinition("bogus"));
    }

}

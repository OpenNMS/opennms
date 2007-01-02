package org.opennms.netmgt.dao.hibernate;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

public class LocationMonitorDaoHibernateTest extends AbstractTransactionalDaoTestCase {

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

    public void testFindStatusChangesForNodeForUniqueMonitorAndInterface() throws InterruptedException {
        OnmsLocationMonitor monitor1 = new OnmsLocationMonitor();
        monitor1.setDefinitionName("Outer Space");
        getLocationMonitorDao().save(monitor1);

        OnmsLocationMonitor monitor2 = new OnmsLocationMonitor();
        monitor2.setDefinitionName("Really Outer Space");
        getLocationMonitorDao().save(monitor2);

        OnmsNode node1 = getNodeDao().get(1);
        assertNotNull("node 1 should not be null", node1);

        OnmsNode node2 = getNodeDao().get(2);
        assertNotNull("node 2 should not be null", node2);
        
        // Add node1/192.168.1.1 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());

        // Add node1/192.168.1.2 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.2").getMonitoredServices());
        
        // Add node1/192.168.1.1 on monitor2
        addStatusChangesForMonitorAndService(monitor2, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        // Add node2/192.168.2.1 on monitor1 to test filtering on a specific node (this shouldn't show up in the results)
        addStatusChangesForMonitorAndService(monitor1, node2.getIpInterfaceByIpAddress("192.168.2.1").getMonitoredServices());

        Thread.sleep(10);
        
        // Add another copy for node1/192.168.1.1 on monitor1 to test distinct
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        Collection<LocationMonitorIpInterface> statuses = getLocationMonitorDao().findStatusChangesForNodeForUniqueMonitorAndInterface(1);
        assertEquals("number of statuses found", 3, statuses.size());

        /*
        for (LocationMonitorIpInterface status : statuses) {
            OnmsLocationMonitor m = status.getLocationMonitor();
            OnmsIpInterface i = status.getIpInterface();
            
            System.err.println("monitor " + m.getId() + " " + m.getDefinitionName() + ", IP " + i.getIpAddress());
        }
        */

    }

    private void addStatusChangesForMonitorAndService(OnmsLocationMonitor monitor, Set<OnmsMonitoredService> services) {
        for (OnmsMonitoredService service : services) {
            OnmsLocationSpecificStatus status = new OnmsLocationSpecificStatus();
            status.setLocationMonitor(monitor);
            status.setMonitoredService(service);
            status.setPollResult(PollStatus.available());
            getLocationMonitorDao().saveStatusChange(status);
            //System.err.println("Adding status for " + status.getMonitoredService() + " from " + status.getLocationMonitor().getId());
        }
    }
}

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Organized imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
import org.springframework.jdbc.core.JdbcTemplate;

public class LocationMonitorDaoHibernateTest extends AbstractTransactionalDaoTestCase {

    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }

    @SuppressWarnings("deprecation")
    public JdbcTemplate _getJdbcTemplate() {
    	return getJdbcTemplate();
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
    	assertEquals(2, _getJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", args));
    	
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
    
    public void testFindStatusChangesForNodeForUniqueMonitorAndInterface() {
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
        
        // Add node1/fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("fe80::aaaa:bbbb:cccc:dddd%5").getMonitoredServices());
        
        // Add node2/192.168.2.1 on monitor1 to test filtering on a specific node (this shouldn't show up in the results)
        addStatusChangesForMonitorAndService(monitor1, node2.getIpInterfaceByIpAddress("192.168.2.1").getMonitoredServices());

        // Add another copy for node1/192.168.1.1 on monitor1 to test distinct
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        Collection<LocationMonitorIpInterface> statuses = getLocationMonitorDao().findStatusChangesForNodeForUniqueMonitorAndInterface(1);
        assertEquals("number of statuses found", 4, statuses.size());

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

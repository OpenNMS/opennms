/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.expect;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SimpleWebTable.Cell;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class DefaultDistributedStatusServiceTest extends TestCase {
    private DefaultDistributedStatusService m_service = new DefaultDistributedStatusService();
    
    private EasyMockUtils m_easyMockUtils = new EasyMockUtils();
    
    private MonitoredServiceDao m_monitoredServiceDao = m_easyMockUtils.createMock(MonitoredServiceDao.class);
    private LocationMonitorDao m_locationMonitorDao = m_easyMockUtils.createMock(LocationMonitorDao.class); 
    private ApplicationDao m_applicationDao = m_easyMockUtils.createMock(ApplicationDao.class);
    private ResourceDao m_resourceDao = m_easyMockUtils.createMock(ResourceDao.class);
    private GraphDao m_graphDao = m_easyMockUtils.createMock(GraphDao.class);

    private OnmsMonitoringLocationDefinition m_locationDefinition1;
    private OnmsMonitoringLocationDefinition m_locationDefinition2;
    private OnmsMonitoringLocationDefinition m_locationDefinition3;
    private OnmsLocationMonitor m_locationMonitor1_1;
    private OnmsLocationMonitor m_locationMonitor2_1;
    private OnmsLocationMonitor m_locationMonitor2_2;
    private OnmsApplication m_application1;
    private OnmsApplication m_application2;
    
    private Collection<OnmsMonitoredService> m_services;
    private OnmsNode m_node;

    private String m_ip;

    private Set<OnmsMonitoredService> m_applicationServices1;

    private Set<OnmsMonitoredService> m_applicationServices2;
    
    private static final SimpleDateFormat s_dbDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
    
    public final static String IGNORE_MATCH = "**IGNORE*MATCH**";
        
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_service.setMonitoredServiceDao(m_monitoredServiceDao);
        m_service.setLocationMonitorDao(m_locationMonitorDao);
        m_service.setApplicationDao(m_applicationDao);
        m_service.setResourceDao(m_resourceDao);
        m_service.setGraphDao(m_graphDao);
        m_service.afterPropertiesSet();
        
        m_locationDefinition1 = new OnmsMonitoringLocationDefinition("Raleigh", "raleigh", "OpenNMS NC");
        m_locationDefinition2 = new OnmsMonitoringLocationDefinition("Durham", "durham", "OpenNMS NC");
        m_locationDefinition3 = new OnmsMonitoringLocationDefinition("Columbus", "columbus", "OpenNMS OH");

        m_application1 = new OnmsApplication();
        m_application1.setName("Application 1");
        
        m_application2 = new OnmsApplication();
        m_application2.setName("Application 2");

        m_locationMonitor1_1 = new OnmsLocationMonitor();
        m_locationMonitor1_1.setId(1);
        m_locationMonitor1_1.setLastCheckInTime(new Date());
        m_locationMonitor1_1.setDefinitionName("Raleigh");
        m_locationMonitor1_1.setStatus(MonitorStatus.STARTED);
        assertEquals("location monitor 1.1 status", MonitorStatus.STARTED, m_locationMonitor1_1.getStatus());
        
        m_locationMonitor2_1 = new OnmsLocationMonitor();
        m_locationMonitor2_1.setId(2);
        m_locationMonitor2_1.setLastCheckInTime(new Date());
        m_locationMonitor2_1.setDefinitionName("Durham");
        m_locationMonitor2_1.setStatus(MonitorStatus.STARTED);
        assertEquals("location monitor 2.1 status", MonitorStatus.STARTED, m_locationMonitor2_1.getStatus());
        
        m_locationMonitor2_2 = new OnmsLocationMonitor();
        m_locationMonitor2_2.setId(3);
        m_locationMonitor2_2.setDefinitionName("Durham");
        m_locationMonitor2_2.setStatus(MonitorStatus.STARTED);
        assertEquals("location monitor 2.2 status", MonitorStatus.STARTED, m_locationMonitor2_2.getStatus());
        
        List<String> serviceNames = new ArrayList<String>();
        serviceNames.add("ICMP");
        serviceNames.add("DNS");
        serviceNames.add("HTTP");
        serviceNames.add("HTTPS");
        Collections.shuffle(serviceNames); // shuffle to test sorting
        
        m_node = new OnmsNode();
        m_ip = "1.1.1.1";
        m_node.setLabel("Node 1");
        m_node.setId(1);
        
        // Can't shuffle since it's a set
        m_services = new HashSet<OnmsMonitoredService>();
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("ICMP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("DNS")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("HTTP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("HTTPS")));

        // Can't shuffle since these since they are sets
        m_applicationServices1 = new TreeSet<OnmsMonitoredService>();
        m_applicationServices1.add(findMonitoredService(m_services, m_ip, "HTTP"));
        m_applicationServices1.add(findMonitoredService(m_services, m_ip, "HTTPS"));
//        m_application1.setMemberServices(applicationServices1);
        
        m_applicationServices2 = new TreeSet<OnmsMonitoredService>();
        m_applicationServices2.add(findMonitoredService(m_services, m_ip, "HTTPS"));
//        m_application2.setMemberServices(applicationServices2);


        /*
        m_application2 = new OnmsApplication();
        m_application2.setLabel("Application 2");
        // XXX shuffle to verify sorting? create new list and do: Collections.shuffle(applicationServices2)
        m_application2.setMemberServices(applicationServices1);
        */

        
    }
    
    public void testFindLocationSpecificStatusNullLocation() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");
        
        command.setApplication(m_application1.getName());
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("location cannot be null"));
        try {
            m_service.findLocationSpecificStatus(command, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusNullApplication() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");
        
        command.setLocation(m_locationDefinition1.getName());
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("application cannot be null"));
        try {
            m_service.findLocationSpecificStatus(command, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusInvalidLocation() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");
        
        command.setLocation("invalid location");
        command.setApplication(m_application1.getName());
        
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(command.getLocation())).andReturn(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Could not find location for location name \"" + command.getLocation() + "\""));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.findLocationSpecificStatus(command, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusInvalidApplication() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");

        command.setLocation(m_locationDefinition1.getName());
        command.setApplication("invalid application");
        
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition1.getName())).andReturn(m_locationDefinition1);
        expect(m_applicationDao.findByName(command.getApplication())).andReturn(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("Could not find application for application name \"" + command.getApplication() + "\""));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.findLocationSpecificStatus(command, errors);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        m_easyMockUtils.verifyAll();
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatus() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");

        command.setLocation(m_locationDefinition1.getName());
        command.setApplication(m_application1.getName());

        expectEverything();
        
        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1);
        
        m_easyMockUtils.replayAll();
        
        List<OnmsLocationSpecificStatus> status =
            m_service.findLocationSpecificStatus(command, errors);

        m_easyMockUtils.verifyAll();
        
        assertEquals("status list size", 2, status.size());
    }
    
    public void testCreateStatus() {
        // We run five times to catch sorting differences (if we don't sort)
        for (int i = 0; i < 5; i++) {
            runTestCreateStatus();
        }
    }
    
    public void runTestCreateStatus() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");

        command.setLocation(m_locationDefinition1.getName());
        command.setApplication(m_application1.getName());

        expectEverything();
        
        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1);
        
        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createStatusTable(command, errors);
        
        m_easyMockUtils.verifyAll();
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed poller view for Application 1 from Raleigh location");
        
        expectedTable.addColumn("Node", "");
        expectedTable.addColumn("Monitor", "");
        expectedTable.addColumn("Service", "");
        expectedTable.addColumn("Status", "");
        expectedTable.addColumn("Response", "");
        expectedTable.addColumn("Last Status Change", "");
        expectedTable.addColumn("Last Update", "");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Normal", "element/node.jsp?node=1");
        expectedTable.addCell("Raleigh-1", "", "distributed/locationMonitorDetails.htm?monitorId=1");
        expectedTable.addCell("HTTP", "", "element/service.jsp?ifserviceid=null");
        expectedTable.addCell("Up", "bright");
        expectedTable.addCell("", "");
        expectedTable.addCell(IGNORE_MATCH, "");
        expectedTable.addCell(IGNORE_MATCH, "");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Indeterminate", "element/node.jsp?node=1");
        expectedTable.addCell("Raleigh-1", "", "distributed/locationMonitorDetails.htm?monitorId=1");
        expectedTable.addCell("HTTPS", "", "element/service.jsp?ifserviceid=null");
        expectedTable.addCell("Unknown", "bright");
        expectedTable.addCell("No status recorded for this service from this location", "");
        expectedTable.addCell(IGNORE_MATCH, "");
        expectedTable.addCell(IGNORE_MATCH, "");
        
        assertTableEquals(expectedTable, table);
    }


    public void testCreateStatusPutUnreportedServicesLast() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");

        command.setLocation(m_locationDefinition1.getName());
        command.setApplication(m_application1.getName());

        expect(m_applicationDao.findByName("Application 1")).andReturn(m_application1);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition1.getName())).andReturn(m_locationDefinition1);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition1)).andReturn(Collections.singleton(m_locationMonitor1_1));

        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpService)).andReturn(null);
        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpsService)).andReturn(new OnmsLocationSpecificStatus(m_locationMonitor1_1, httpsService, PollStatus.available()));
        
        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1);
        
        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createStatusTable(command, errors);
        
        m_easyMockUtils.verifyAll();
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed poller view for Application 1 from Raleigh location");
        
        expectedTable.addColumn("Node", "");
        expectedTable.addColumn("Monitor", "");
        expectedTable.addColumn("Service", "");
        expectedTable.addColumn("Status", "");
        expectedTable.addColumn("Response", "");
        expectedTable.addColumn("Last Status Change", "");
        expectedTable.addColumn("Last Update", "");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Normal", "element/node.jsp?node=1");
        expectedTable.addCell("Raleigh-1", "", "distributed/locationMonitorDetails.htm?monitorId=1");
        expectedTable.addCell("HTTPS", "", "element/service.jsp?ifserviceid=null");
        expectedTable.addCell("Up", "bright");
        expectedTable.addCell("", "");
        expectedTable.addCell(IGNORE_MATCH, "");
        expectedTable.addCell(IGNORE_MATCH, "");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Indeterminate", "element/node.jsp?node=1");
        expectedTable.addCell("Raleigh-1", "", "distributed/locationMonitorDetails.htm?monitorId=1");
        expectedTable.addCell("HTTP", "", "element/service.jsp?ifserviceid=null");
        expectedTable.addCell("Unknown", "bright");
        expectedTable.addCell("No status recorded for this service from this location", "");
        expectedTable.addCell(IGNORE_MATCH, "");
        expectedTable.addCell(IGNORE_MATCH, "");
        
        assertTableEquals(expectedTable, table);
    }

    
    
    public void testCreateStatusNoLocationMonitor() {
        DistributedStatusDetailsCommand command = new DistributedStatusDetailsCommand();
        Errors errors = new BindException(command, "command");

        command.setLocation(m_locationDefinition3.getName());
        command.setApplication(m_application2.getName());

        expect(m_applicationDao.findByName("Application 2")).andReturn(m_application2);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition3.getName())).andReturn(m_locationDefinition3);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition3)).andReturn(new HashSet<OnmsLocationMonitor>());

        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createStatusTable(command, errors);

        Errors errorsOut = table.getErrors();
        assertEquals("Number of errors", 1, errorsOut.getErrorCount());
        assertEquals("Number of global errors", 1, errorsOut.getGlobalErrorCount());
        assertEquals("Number of field errors", 0, errorsOut.getFieldErrorCount());
        ObjectError e = (ObjectError) errorsOut.getGlobalErrors().get(0);
        assertEquals("Error code 0", "location.no-monitors", e.getCode());
        assertEquals("Error 0 argument count", 2, e.getArguments().length);
        assertEquals("Error argument 0.0", "Application 2", e.getArguments()[0]);
        assertEquals("Error argument 0.0", "Columbus", e.getArguments()[1]);

        m_easyMockUtils.verifyAll();
    }

    public void testCreateFacilityStatusTableNoStartDate() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument start cannot be null"));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.createFacilityStatusTable(null, new Date());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_easyMockUtils.verifyAll();
    }

    public void testCreateFacilityStatusTableNoEndDate() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument end cannot be null"));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.createFacilityStatusTable(new Date(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_easyMockUtils.verifyAll();
    }

    public void testCreateFacilityStatusTableStateDateNotBefore() {
        Date start = new Date();
        Date end = new Date(start.getTime() - 1000); // one second before start time

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("start date (" + start + ") must be older than end date (" + end + ")"));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.createFacilityStatusTable(start, end);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_easyMockUtils.verifyAll();
    }

    public void testCreateFacilityStatusTableDatesEqual() {
        Date startAndEnd = new Date();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("start date (" + startAndEnd + ") must be older than end date (" + startAndEnd + ")"));
        
        m_easyMockUtils.replayAll();
        try {
            m_service.createFacilityStatusTable(startAndEnd, startAndEnd);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_easyMockUtils.verifyAll();
    }

    public void testCreateFacilityStatusTableNoApplications() {
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(Collections.singletonList(m_locationDefinition1));
        expect(m_applicationDao.findAll()).andReturn(new ArrayList<OnmsApplication>());
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("there are no applications"));

        Date start = new Date();
        Date end = new Date(start.getTime() + 1000); // one second after start time

        m_easyMockUtils.replayAll();
        try {
            m_service.createFacilityStatusTable(start, end);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_easyMockUtils.verifyAll();
    }
    
    public void testCreateFacilityStatusTable() throws Exception {
         // No need to shuffle, since this is a list
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        
        List<OnmsApplication> applications = new LinkedList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        Collections.shuffle(applications);
        
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");
        OnmsMonitoredService icmpService = findMonitoredService(m_services, m_ip, "ICMP");
        
        Collection<OnmsLocationSpecificStatus> mostRecentStatuses = new LinkedList<OnmsLocationSpecificStatus>();
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpsService, PollStatus.available(), "20061012-06:00:00"));
        
        Collection<OnmsLocationSpecificStatus> statusChanges = new LinkedList<OnmsLocationSpecificStatus>();
        statusChanges.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.unavailable(), "20061012-00:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, icmpService, PollStatus.down(), "20061010-06:00:00"));

        Date startDate = s_dbDate.parse("2006-10-12 00:00:00.0");
        Date endDate = s_dbDate.parse("2006-10-13 00:00:00.0");

        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        expect(m_applicationDao.findAll()).andReturn(applications);
        expect(m_locationMonitorDao.getAllMostRecentStatusChanges()).andReturn(mostRecentStatuses);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(0))).andReturn(Collections.singleton(m_locationMonitor1_1));
        Collection<OnmsLocationMonitor> monitors2 = new HashSet<OnmsLocationMonitor>();
        monitors2.add(m_locationMonitor2_1);
        monitors2.add(m_locationMonitor2_2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(1))).andReturn(monitors2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(2))).andReturn(new HashSet<OnmsLocationMonitor>());
        expect(m_locationMonitorDao.getStatusChangesBetween(startDate, endDate)).andReturn(statusChanges);
        expect(m_locationMonitorDao.getAllStatusChangesAt(startDate)).andReturn(new HashSet<OnmsLocationSpecificStatus>());

        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1).times(3);
        expect(m_monitoredServiceDao.findByApplication(m_application2)).andReturn(m_applicationServices2).times(3);

        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createFacilityStatusTable(startDate, endDate);
        m_easyMockUtils.verifyAll();
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed Poller Status Summary");
        expectedTable.addColumn("Area", "");
        expectedTable.addColumn("Location", "");
        expectedTable.addColumn("Application 1", "");
        expectedTable.addColumn("Application 2", "");

        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "");
        expectedTable.addCell("Raleigh", "");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusHistory.htm?location=Raleigh&application=Application+1");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusHistory.htm?location=Raleigh&application=Application+2");
        
        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "");
        expectedTable.addCell("Durham", "");
        expectedTable.addCell("No data", "Normal");
        expectedTable.addCell("No data", "Normal");
        
        expectedTable.newRow();
        expectedTable.addCell("OpenNMS OH", "");
        expectedTable.addCell("Columbus", "");
        expectedTable.addCell("No data", "Indeterminate");
        expectedTable.addCell("No data", "Indeterminate");
        
        assertTableEquals(expectedTable, table);
    }
    

    
    /*
     * XXX need to check sorting
     */
    public void testCreateFacilityStatusTableOneApplicationOneOfTwoLocationsReporting() throws Exception {
        OnmsApplication app = m_application2;
        
        // No need to shuffle, since this is a list
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");
        
        Collection<OnmsLocationSpecificStatus> mostRecentStatuses = new LinkedList<OnmsLocationSpecificStatus>();
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061011-00:00:00"));
        
        Collection<OnmsLocationSpecificStatus> statusChanges = new LinkedList<OnmsLocationSpecificStatus>();
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061011-00:00:00"));

        Date startDate = s_dbDate.parse("2006-10-12 00:00:00.0");
        Date endDate = s_dbDate.parse("2006-10-13 00:00:00.0");

        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        expect(m_applicationDao.findAll()).andReturn(Collections.singletonList(app));
        expect(m_locationMonitorDao.getAllMostRecentStatusChanges()).andReturn(mostRecentStatuses);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(0))).andReturn(Collections.singleton(m_locationMonitor1_1));
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(1))).andReturn(Collections.singleton(m_locationMonitor2_1));
        expect(m_locationMonitorDao.getStatusChangesBetween(startDate, endDate)).andReturn(statusChanges);
        expect(m_locationMonitorDao.getAllStatusChangesAt(startDate)).andReturn(new HashSet<OnmsLocationSpecificStatus>());

        expect(m_monitoredServiceDao.findByApplication(app)).andReturn(m_applicationServices2).times(2);

        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createFacilityStatusTable(startDate, endDate);
        m_easyMockUtils.verifyAll();
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed Poller Status Summary");
        expectedTable.addColumn("Area", "");
        expectedTable.addColumn("Location", "");
        expectedTable.addColumn(app.getName(), "");

        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "");
        expectedTable.addCell("Raleigh", "");
        expectedTable.addCell("100.000%", "Normal", "distributedStatusHistory.htm?location=Raleigh&application=Application+2");
        
        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "");
        expectedTable.addCell("Durham", "");
        expectedTable.addCell("No data", "Indeterminate");
        
        assertTableEquals(expectedTable, table);
    }
    /*
     * XXX need to check sorting
     */
    public void testCreateFacilityStatusTableLayoutApplicationsVertically() throws Exception {
        // No need to shuffle, since this is a list
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        
        List<OnmsApplication> applications = new LinkedList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        Collections.shuffle(applications);
        
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");
        OnmsMonitoredService icmpService = findMonitoredService(m_services, m_ip, "ICMP");
        
        Collection<OnmsLocationSpecificStatus> mostRecentStatuses = new LinkedList<OnmsLocationSpecificStatus>();
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpsService, PollStatus.available(), "20061012-06:00:00"));
        
        Collection<OnmsLocationSpecificStatus> statusChanges = new LinkedList<OnmsLocationSpecificStatus>();
        statusChanges.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.unavailable(), "20061012-00:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, icmpService, PollStatus.down(), "20061010-06:00:00"));

        Date startDate = s_dbDate.parse("2006-10-12 00:00:00.0");
        Date endDate = s_dbDate.parse("2006-10-13 00:00:00.0");

        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        expect(m_applicationDao.findAll()).andReturn(applications);
        expect(m_locationMonitorDao.getAllMostRecentStatusChanges()).andReturn(mostRecentStatuses);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(0))).andReturn(Collections.singleton(m_locationMonitor1_1));
        Collection<OnmsLocationMonitor> monitors2 = new HashSet<OnmsLocationMonitor>();
        monitors2.add(m_locationMonitor2_1);
        monitors2.add(m_locationMonitor2_2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(1))).andReturn(monitors2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(2))).andReturn(new HashSet<OnmsLocationMonitor>());
        expect(m_locationMonitorDao.getStatusChangesBetween(startDate, endDate)).andReturn(statusChanges);
        expect(m_locationMonitorDao.getAllStatusChangesAt(startDate)).andReturn(new HashSet<OnmsLocationSpecificStatus>());

        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1).times(3);
        expect(m_monitoredServiceDao.findByApplication(m_application2)).andReturn(m_applicationServices2).times(3);
        
        m_service.setLayoutApplicationsVertically(true);

        m_easyMockUtils.replayAll();
        SimpleWebTable table = m_service.createFacilityStatusTable(startDate, endDate);
        m_easyMockUtils.verifyAll();
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed Poller Status Summary");
        expectedTable.addColumn("Application", "");
        expectedTable.addColumn("Raleigh", "");
        expectedTable.addColumn("Durham", "");
        expectedTable.addColumn("Columbus", "");
        
        
        expectedTable.newRow();
        expectedTable.addCell("Application 1", "");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusHistory.htm?location=Raleigh&application=Application+1");
        expectedTable.addCell("No data", "Normal");
        expectedTable.addCell("No data", "Indeterminate");
        
        expectedTable.newRow();
        expectedTable.addCell("Application 2", "");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusHistory.htm?location=Raleigh&application=Application+2");
        expectedTable.addCell("No data", "Normal");
        expectedTable.addCell("No data", "Indeterminate");
        
        assertTableEquals(expectedTable, table);
    }
    
    
    public void testPercentageCalculationAllAvailableStartInMiddleOfDay() throws ParseException {
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        Collection<OnmsLocationSpecificStatus> statuses = new HashSet<OnmsLocationSpecificStatus>();
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061012-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061013-00:00:00"));

        Date startDate = s_dbDate.parse("2006-10-12 00:00:00.0");
        Date endDate = s_dbDate.parse("2006-10-13 00:00:00.0");

        m_easyMockUtils.replayAll();
        String percentage = m_service.calculatePercentageUptime(m_applicationServices1, statuses,  startDate, endDate);
        m_easyMockUtils.verifyAll();
        
        assertEquals("percentage", "100.000%", percentage);
    }
    
    public void testPercentageCalculationOneUnavailableThenAvailaleInMiddleOfDay() throws ParseException {
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        Collection<OnmsLocationSpecificStatus> statuses = new HashSet<OnmsLocationSpecificStatus>();
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061012-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.unavailable(), "20061012-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061013-00:00:00"));

        Date startDate = s_dbDate.parse("2006-10-12 00:00:00.0");
        Date endDate = s_dbDate.parse("2006-10-13 00:00:00.0");

        m_easyMockUtils.replayAll();
        String percentage = m_service.calculatePercentageUptime(m_applicationServices1, statuses,  startDate, endDate);
        m_easyMockUtils.verifyAll();
        
        assertEquals("percentage", "75.000%", percentage);
    }
    
    public void testDetails() {
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        
        List<OnmsApplication> applications = new ArrayList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        expect(m_applicationDao.findAll()).andReturn(applications);
        
        expect(m_locationMonitorDao.findMonitoringLocationDefinition("Durham")).andReturn(m_locationDefinition2);
        expect(m_applicationDao.findByName("Application 2")).andReturn(m_application2);
        
        List<OnmsLocationMonitor> monitors = new ArrayList<OnmsLocationMonitor>();
        monitors.add(m_locationMonitor2_1);
        monitors.add(m_locationMonitor2_2);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition2)).andReturn(monitors);
        
        for (OnmsMonitoredService service : m_applicationServices2) {
            m_locationMonitorDao.initialize(service.getIpInterface());
            m_locationMonitorDao.initialize(service.getIpInterface().getNode());
        }
        
        String locationName = m_locationDefinition2.getName();
        String applicationName = m_application2.getName();
        String monitorId = "";
        String timeSpan = "Last Day";
        String previousLocation = "";
        
        expect(m_monitoredServiceDao.findByApplication(m_application2)).andReturn(m_applicationServices2).times(2);
        
        expectResourceDaoCall(m_locationMonitor2_1, m_applicationServices2);
        
        m_easyMockUtils.replayAll();
        DistributedStatusHistoryModel summary =  m_service.createHistoryModel(locationName, monitorId, applicationName, timeSpan, previousLocation);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("summary should not be null", summary);
        assertNotNull("summary locations list should not be null", summary.getLocations());
        assertNotNull("summary applications list should not be null", summary.getApplications());
        assertNotNull("summary chosen location should not be null", summary.getChosenLocation());
        assertNotNull("summary chosen application should not be null", summary.getChosenApplication());
        assertNotNull("summary error list should not be null", summary.getErrors());
        
        assertEquals("summary locations list size", locationDefinitions.size(), summary.getLocations().size());
        assertEquals("summary applications list size", applications.size(), summary.getApplications().size());
        assertEquals("summary error list size: " + summary.getErrors(), 0, summary.getErrors().size());
        
        // Verify sorting of applications
        assertEquals("summary applications 1", m_application1, summary.getApplications().get(0));
        assertEquals("summary applications 2", m_application2, summary.getApplications().get(1));
        
        // Verify chosen ones
        assertEquals("summary chosen location", m_locationDefinition2, summary.getChosenLocation());
        assertEquals("summary chosen application", m_application2, summary.getChosenApplication());
        
        // And verify that they are in the lists in the right place
        assertEquals("summary chosen location matches list", summary.getLocations().get(1), summary.getChosenLocation());
        assertEquals("summary chosen application matches list", summary.getApplications().get(1), summary.getChosenApplication());
        
        assertEquals("graph URL map size", 1, summary.getServiceGraphs().size());
        assertNotNull("graph 0 URL should not be null", summary.getServiceGraphs().iterator().next().getUrl());
    }

    private void expectResourceDaoCall(OnmsLocationMonitor monitor, Collection<OnmsMonitoredService> services) {
        PrefabGraph httpGraph = new PrefabGraph("http", "title", new String[] { "http" }, "command", new String[0], new String[0], 0, new String[] { "distributedStatus" }, null, null, null, new String[0]);
        PrefabGraph httpsGraph = new PrefabGraph("https", "title", new String[] { "https" }, "command", new String[0], new String[0], 0, new String[] { "distributedStatus" }, null, null, null, new String[0]);
        
        for (OnmsMonitoredService service : services) {
            OnmsResource resource = new OnmsResource("foo", "even more foo", new MockResourceType(), new HashSet<OnmsAttribute>(0));
            expect(m_resourceDao.getResourceForIpInterface(service.getIpInterface(), monitor)).andReturn(resource);
            
            expect(m_graphDao.getPrefabGraphsForResource(resource)).andReturn(new PrefabGraph[] { httpGraph, httpsGraph });
        }
        
        expect(m_graphDao.getPrefabGraph(httpGraph.getName())).andReturn(httpGraph).anyTimes();
        expect(m_graphDao.getPrefabGraph(httpsGraph.getName())).andReturn(httpsGraph).atLeastOnce();
    }
    
    public void testWrongLocationDetails() {
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        
        List<OnmsApplication> applications = new ArrayList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        expect(m_applicationDao.findAll()).andReturn(applications);
        
        expect(m_locationMonitorDao.findMonitoringLocationDefinition("Raleigh-bad")).andReturn(null);
        expect(m_applicationDao.findByName("Application 2")).andReturn(m_application2);
        
        List<OnmsLocationMonitor> monitors = new ArrayList<OnmsLocationMonitor>();
        monitors.add(m_locationMonitor1_1);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition1)).andReturn(monitors);
        
        for (OnmsMonitoredService service : m_applicationServices2) {
            m_locationMonitorDao.initialize(service.getIpInterface());
            m_locationMonitorDao.initialize(service.getIpInterface().getNode());
        }

        String locationName = "Raleigh-bad";
        String applicationName = m_application2.getName();
        String monitorId = "";
        String previousLocation = "";
        String timeSpan = "";
        
        expect(m_monitoredServiceDao.findByApplication(m_application2)).andReturn(m_applicationServices2).times(2);
        
        expectResourceDaoCall(m_locationMonitor1_1, m_applicationServices2);
        
        m_easyMockUtils.replayAll();
        DistributedStatusHistoryModel summary = m_service.createHistoryModel(locationName, monitorId, applicationName, timeSpan, previousLocation);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("summary should not be null", summary);
        assertNotNull("summary locations list should not be null", summary.getLocations());
        assertNotNull("summary applications list should not be null", summary.getApplications());
        assertNotNull("summary chosen location should not be null", summary.getChosenLocation());
        assertNotNull("summary chosen application should not be null", summary.getChosenApplication());
        assertNotNull("summary error list should not be null", summary.getErrors());
        
        assertEquals("summary locations list size", locationDefinitions.size(), summary.getLocations().size());
        assertEquals("summary applications list size", applications.size(), summary.getApplications().size());
        assertEquals("summary error list size", 1, summary.getErrors().size());
        
        // Verify sorting of applications
        assertEquals("summary applications 1", m_application1, summary.getApplications().get(0));
        assertEquals("summary applications 2", m_application2, summary.getApplications().get(1));
        
        // Verify errors
        assertEquals("summary error 1", "Could not find location definition 'Raleigh-bad'", summary.getErrors().get(0));
        
        // Verify chosen ones
        assertEquals("summary chosen location", m_locationDefinition1, summary.getChosenLocation());
        assertEquals("summary chosen application", m_application2, summary.getChosenApplication());
        
        // And verify that they are in the lists in the right place
        assertEquals("summary chosen location matches list", summary.getLocations().get(0), summary.getChosenLocation());
        assertEquals("summary chosen application matches list", summary.getApplications().get(1), summary.getChosenApplication());

    }
    
    public void testWrongApplicationDetails() {
        List<OnmsMonitoringLocationDefinition> locationDefinitions = new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        
        List<OnmsApplication> applications = new ArrayList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        expect(m_applicationDao.findAll()).andReturn(applications);
        
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition2.getName())).andReturn(m_locationDefinition2);
        expect(m_applicationDao.findByName("Big Bad Voodoo Daddy Application")).andReturn(null);
        
        List<OnmsLocationMonitor> monitors = new ArrayList<OnmsLocationMonitor>();
        monitors.add(m_locationMonitor2_1);
        monitors.add(m_locationMonitor2_2);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition2)).andReturn(monitors);

        for (OnmsMonitoredService service : m_applicationServices1) {
            m_locationMonitorDao.initialize(service.getIpInterface());
            m_locationMonitorDao.initialize(service.getIpInterface().getNode());
        }

        String locationName = m_locationDefinition2.getName();
        String applicationName = "Big Bad Voodoo Daddy Application";
        String monitorId = "";
        String previousLocation = "";
        String timeSpan = "";
        
        expect(m_monitoredServiceDao.findByApplication(m_application1)).andReturn(m_applicationServices1).times(2);

        expectResourceDaoCall(m_locationMonitor2_1, m_applicationServices1);

        m_easyMockUtils.replayAll();
        DistributedStatusHistoryModel summary = m_service.createHistoryModel(locationName, monitorId, applicationName, timeSpan, previousLocation);
        m_easyMockUtils.verifyAll();
        
        assertNotNull("summary should not be null", summary);
        assertNotNull("summary locations list should not be null", summary.getLocations());
        assertNotNull("summary applications list should not be null", summary.getApplications());
        assertNotNull("summary chosen location should not be null", summary.getChosenLocation());
        assertNotNull("summary chosen application should not be null", summary.getChosenApplication());
        assertNotNull("summary error list should not be null", summary.getErrors());
        
        assertEquals("summary locations list size", locationDefinitions.size(), summary.getLocations().size());
        assertEquals("summary applications list size", applications.size(), summary.getApplications().size());
        assertEquals("summary error list size", 1, summary.getErrors().size());
        
        // Verify sorting of applications
        assertEquals("summary applications 1", m_application1, summary.getApplications().get(0));
        assertEquals("summary applications 2", m_application2, summary.getApplications().get(1));
        
        // Verify errors
        assertEquals("summary error 1", "Could not find application 'Big Bad Voodoo Daddy Application'", summary.getErrors().get(0));
        
        // Verify chosen ones
        assertEquals("summary chosen location", m_locationDefinition2, summary.getChosenLocation());
        assertEquals("summary chosen application", m_application1, summary.getChosenApplication());
        
        // And verify that they are in the lists in the right place
        assertEquals("summary chosen location matches list", summary.getLocations().get(1), summary.getChosenLocation());
        assertEquals("summary chosen application matches list", summary.getApplications().get(0), summary.getChosenApplication());
    }

    private OnmsLocationSpecificStatus createStatus(OnmsLocationMonitor locationMonitor,
            OnmsMonitoredService service, PollStatus status, String timestamp) {
        
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        try {
            status.setTimestamp(f.parse(timestamp));
        } catch (ParseException e) {
            AssertionFailedError error = new AssertionFailedError("Could not parse timestamp \"" + timestamp + "\"");
            error.initCause(e);
            throw error;
        }
        return new OnmsLocationSpecificStatus(locationMonitor, service, status);
    }
    
    public void assertTableEquals(SimpleWebTable expectedTable, SimpleWebTable table) {
        assertEquals("table title", expectedTable.getTitle(), table.getTitle());
        
        assertEquals("number of table columns headers", expectedTable.getColumnHeaders().size(), table.getColumnHeaders().size());
        ListIterator<Cell> columnHeaderIterator = expectedTable.getColumnHeaders().listIterator();
        for (Cell tableColumnHeader : table.getColumnHeaders()) {
            assertEquals("column header " + (columnHeaderIterator.nextIndex() + 1), columnHeaderIterator.next(), tableColumnHeader);
        }
        
        assertEquals("number of rows", expectedTable.getRows().size(), table.getRows().size());
        
        ListIterator<List<Cell>> expectedRowIterator = expectedTable.getRows().listIterator();
        for (List<Cell> row : table.getRows()) {
            List<Cell> expectedRow = expectedRowIterator.next();

            assertEquals("row " + (expectedRowIterator.previousIndex() + 1) + " column count", expectedRow.size(), row.size());

            ListIterator<Cell> expectedColumnIterator = expectedRow.listIterator();
            for (Cell column : row) {
                Cell expectedColumn = expectedColumnIterator.next();
                
                String rowColumn = "row "
                    + (expectedRowIterator.previousIndex() + 1) + " column "
                    + (expectedColumnIterator.previousIndex() + 1) + " "; 
                
                if (!IGNORE_MATCH.equals(expectedColumn.getContent())) {
                    assertEquals(rowColumn + "content",
                                 expectedColumn.getContent(),
                                 column.getContent());
                }
                if (!IGNORE_MATCH.equals(expectedColumn.getStyleClass())) {
                    assertEquals(rowColumn + "styleClass",
                                 expectedColumn.getStyleClass(),
                                   column.getStyleClass());
                }
                if (!IGNORE_MATCH.equals(expectedColumn.getLink())) {
                    assertEquals(rowColumn + "link",
                                 expectedColumn.getLink(),
                                 column.getLink());
                }
            }
        }
    }

    public void expectEverything() {
        expect(m_applicationDao.findByName("Application 1")).andReturn(m_application1);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition1.getName())).andReturn(m_locationDefinition1);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition1)).andReturn(Collections.singleton(m_locationMonitor1_1));

        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpService)).andReturn(new OnmsLocationSpecificStatus(m_locationMonitor1_1, httpService, PollStatus.available()));
        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpsService)).andReturn(null);
    }

    public OnmsMonitoredService findMonitoredService(Collection<OnmsMonitoredService> services, String interfaceIp, String serviceName) {
        return findMonitoredService(services, addr(interfaceIp), serviceName);
    }

    private OnmsMonitoredService findMonitoredService(Collection<OnmsMonitoredService> services, InetAddress ipaddr, String serviceName) {
        for (OnmsMonitoredService service : services) {
            if (ipaddr.equals(service.getIpAddress()) && serviceName.equals(service.getServiceName())) {
                return service;
            }
        }
        
        fail("Could not find service \"" + serviceName + "\" on interface \"" + ipaddr + "\"");
        
        // This will never be reached due to the above fail()
        return null;
    }

}

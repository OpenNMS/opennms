package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.text.DateFormat;
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

import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SimpleWebTable.Cell;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class DefaultDistributedStatusServiceTest extends TestCase {
    private DefaultDistributedStatusService m_service = new DefaultDistributedStatusService();
    
    private PollerConfig m_pollerConfig = createMock(PollerConfig.class);
    private MonitoredServiceDao m_monitoredServiceDao = createMock(MonitoredServiceDao.class);
    private LocationMonitorDao m_locationMonitorDao = createMock(LocationMonitorDao.class); 
    private ApplicationDao m_applicationDao = createMock(ApplicationDao.class);

    private OnmsMonitoringLocationDefinition m_locationDefinition1;
    private OnmsMonitoringLocationDefinition m_locationDefinition2;
    private OnmsMonitoringLocationDefinition m_locationDefinition3;
    private OnmsLocationMonitor m_locationMonitor1_1;
    private OnmsLocationMonitor m_locationMonitor2_1;
    private OnmsLocationMonitor m_locationMonitor2_2;
    private OnmsLocationMonitor m_locationMonitor3_1;
    private OnmsApplication m_application1;
    private OnmsApplication m_application2;
    
    private Package m_pkg;
    private ServiceSelector m_selector;
    private Collection<OnmsMonitoredService> m_services;
    private OnmsNode m_node;

    private String m_ip;
        
    protected void setUp() {
        m_service.setPollerConfig(m_pollerConfig);
        m_service.setMonitoredServiceDao(m_monitoredServiceDao);
        m_service.setLocationMonitorDao(m_locationMonitorDao);
        m_service.setApplicationDao(m_applicationDao);
        
        m_locationDefinition1 = new OnmsMonitoringLocationDefinition("Raleigh", "raleigh", "OpenNMS NC");
        m_locationDefinition2 = new OnmsMonitoringLocationDefinition("Durham", "durham", "OpenNMS NC");
        m_locationDefinition3 = new OnmsMonitoringLocationDefinition("Columbus", "columbus", "OpenNMS OH");

        m_application1 = new OnmsApplication();
        m_application1.setName("Application 1");
        
        m_application2 = new OnmsApplication();
        m_application2.setName("Application 2");

        m_locationMonitor1_1 = new OnmsLocationMonitor();
        m_locationMonitor1_1.setLastCheckInTime(new Date());
        m_locationMonitor1_1.setDefinitionName("Raleigh");
        
        m_locationMonitor2_1 = new OnmsLocationMonitor();
        m_locationMonitor2_1.setLastCheckInTime(new Date());
        m_locationMonitor2_1.setDefinitionName("Durham");
        
        m_locationMonitor2_2 = new OnmsLocationMonitor();
        m_locationMonitor2_2.setDefinitionName("Durham");
        
        m_locationMonitor3_1 = null; // Test the case where there is no monitor for this location

        m_pkg = new Package();

        List<String> serviceNames = new ArrayList<String>();
        serviceNames.add("ICMP");
        serviceNames.add("DNS");
        serviceNames.add("HTTP");
        serviceNames.add("HTTPS");
        Collections.shuffle(serviceNames); // shuffle to test sorting

        m_selector = new ServiceSelector("IPADDR IPLIKE *.*.*.*", serviceNames);
        
        m_node = new OnmsNode();
        m_ip = "1.1.1.1";
        m_node.setLabel("Node 1");
        
        // Can't shuffle since it's a set
        m_services = new HashSet<OnmsMonitoredService>();
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("ICMP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("DNS")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("HTTP")));
        m_services.add(new OnmsMonitoredService(new OnmsIpInterface(m_ip, m_node), new OnmsServiceType("HTTPS")));

        // Can't shuffle since these since they are sets
        Set<OnmsMonitoredService> applicationServices1 = new HashSet<OnmsMonitoredService>();
        applicationServices1.add(findMonitoredService(m_services, m_ip, "HTTP"));
        applicationServices1.add(findMonitoredService(m_services, m_ip, "HTTPS"));
        m_application1.setMemberServices(applicationServices1);
        
        Set<OnmsMonitoredService> applicationServices2 = new HashSet<OnmsMonitoredService>();
        applicationServices2.add(findMonitoredService(m_services, m_ip, "HTTPS"));
        m_application2.setMemberServices(applicationServices2);


        /*
        m_application2 = new OnmsApplication();
        m_application2.setLabel("Application 2");
        // XXX shuffle to verify sorting? create new list and do: Collections.shuffle(applicationServices2)
        m_application2.setMemberServices(applicationServices1);
        */

        
    }
    
    public void testFindLocationSpecificStatusNullLocation() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("locationName cannot be null"));
        try {
            m_service.findLocationSpecificStatus(null, m_application1.getName());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testFindLocationSpecificStatusNullApplication() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("applicationLabel cannot be null"));
        try {
            m_service.findLocationSpecificStatus(m_locationDefinition1.getName(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    // XXX need to figure out what we should be doing here, if anything
    public void XXXtestFindLocationSpecificStatusInvalidLocation() {
        m_service.findLocationSpecificStatus("invalid location", m_application1.getName());
    }
    
    // XXX need to figure out what we should be doing here, if anything
    public void XXXtestFindLocationSpecificStatusInvalidApplication() {
        m_service.findLocationSpecificStatus(m_locationDefinition1.getName(), "invalid application");
    }
    
    public void testFindLocationSpecificStatus() {
        expectEverything();
        
        replayEverything();
        
        List<OnmsLocationSpecificStatus> status =
            m_service.findLocationSpecificStatus(m_locationDefinition1.getName(),
                                                 m_application1.getName());

        verifyEverything();
        
        assertEquals("status list size", 2, status.size());
    }
    
    public void testCreateStatus() {
        // We run five times to catch sorting differences (if we don't sort)
        for (int i = 0; i < 5; i++) {
            runTestCreateStatus();
        }
    }
    
    public void runTestCreateStatus() {
        expectEverything();
        
        replayEverything();
        SimpleWebTable table =
            m_service.createStatusTable(m_locationDefinition1.getName(),
                                        m_application1.getName());
        
        verifyEverything();
        
        //System.out.print(table.toString());
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed poller view for Application 1 from Raleigh location");
        
        expectedTable.addColumn("Node", "simpleWebTableHeader");
        expectedTable.addColumn("Monitor", "simpleWebTableHeader");
        expectedTable.addColumn("Service", "simpleWebTableHeader");
        expectedTable.addColumn("Status", "simpleWebTableHeader");
        expectedTable.addColumn("Response Time", "simpleWebTableHeader");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Normal");
        expectedTable.addCell("Raleigh-null", "");
        expectedTable.addCell("HTTP", "");
        expectedTable.addCell("Up", "bright");
        expectedTable.addCell("", "");
        expectedTable.newRow();
        
        expectedTable.addCell("Node 1", "Critical");
        expectedTable.addCell("Raleigh-null", "");
        expectedTable.addCell("HTTPS", "");
        expectedTable.addCell("Unknown", "bright");
        expectedTable.addCell("", "");
        
        assertTableEquals(expectedTable, table);
    }

    
    public void testCreateStatusNoLocationMonitor() {
        // We run five times to catch sorting differences (if we don't sort)
        for (int i = 0; i < 5; i++) {
            runTestCreateStatusNoLocationMonitor();
        }
    }

    
    public void runTestCreateStatusNoLocationMonitor() {
        //expectEverything();
        resetEverything();
        
        expect(m_applicationDao.findByName("Application 2")).andReturn(m_application2);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition3.getName())).andReturn(m_locationDefinition3);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition3)).andReturn(Collections.EMPTY_SET);
        //expect(m_pollerConfig.getPackage("columbus")).andReturn(m_pkg);
        //expect(m_pollerConfig.getServiceSelectorForPackage(m_pkg)).andReturn(m_selector);
        //expect(m_monitoredServiceDao.findMatchingServices(m_selector)).andReturn(m_services);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("No location monitors have registered for location \"Columbus\""));
        
        replayEverything();
        try {
            SimpleWebTable table =
                m_service.createStatusTable(m_locationDefinition3.getName(),
                                            m_application2.getName());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

        verifyEverything();

        /*
        
        
        //System.out.print(table.toString());
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed poller view for Application 2 from Columbus location");
        
        expectedTable.addColumn("Node", "simpleWebTableHeader");
        expectedTable.addColumn("Monitor", "simpleWebTableHeader");
        expectedTable.addColumn("Service", "simpleWebTableHeader");
        expectedTable.addColumn("Status", "simpleWebTableHeader");
        expectedTable.addColumn("Response Time", "simpleWebTableHeader");
        
        expectedTable.newRow();
        expectedTable.addCell("Node 1", "Critical");
        expectedTable.addCell("Node 1", "");
        expectedTable.addCell("HTTPS", "");
        expectedTable.addCell("Unknown", "bright");
        expectedTable.addCell("", "");
        
        assertTableEquals(expectedTable, table);
        */
    }

    public void testCreateFacilityStatusTable() {
        for (int i = 0; i < 5; i++) {
            runTestCreateFacilityStatusTable();
        }
    }
    
    
    /*
     * XXX need to check sorting
     */
    public void runTestCreateFacilityStatusTable() {
        resetEverything();
        
        // No need to shuffle, since this is a list
        List<OnmsMonitoringLocationDefinition> locationDefinitions =
            new LinkedList<OnmsMonitoringLocationDefinition>();
        locationDefinitions.add(m_locationDefinition1);
        locationDefinitions.add(m_locationDefinition2);
        locationDefinitions.add(m_locationDefinition3);
        
        List<OnmsApplication> applications =
            new LinkedList<OnmsApplication>();
        applications.add(m_application1);
        applications.add(m_application2);
        Collections.shuffle(applications);
        
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");
        
        Collection<OnmsLocationSpecificStatus> mostRecentStatuses =
            new LinkedList<OnmsLocationSpecificStatus>();
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpService, PollStatus.available(), "20061011-00:00:00"));
        mostRecentStatuses.add(createStatus(m_locationMonitor2_2, httpsService, PollStatus.available(), "20061012-06:00:00"));
        
        Collection<OnmsLocationSpecificStatus> statusChanges =
            new LinkedList<OnmsLocationSpecificStatus>();
        statusChanges.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statusChanges.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));

        Date startDate = new Date(2006 - 1900, 10 - 1, 12, 0, 0, 0);
        Date endDate = new Date(2006 - 1900, 10 - 1, 13, 0, 0, 0);

        
        expect(m_locationMonitorDao.findAllMonitoringLocationDefinitions()).andReturn(locationDefinitions);
        expect(m_applicationDao.findAll()).andReturn(applications);
        expect(m_locationMonitorDao.getAllMostRecentStatusChanges()).andReturn(mostRecentStatuses);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(0))).andReturn(Collections.singleton(m_locationMonitor1_1));
        Collection<OnmsLocationMonitor> monitors2 = new HashSet<OnmsLocationMonitor>();
        monitors2.add(m_locationMonitor2_1);
        monitors2.add(m_locationMonitor2_2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(1))).andReturn(monitors2);
        expect(m_locationMonitorDao.findByLocationDefinition(locationDefinitions.get(2))).andReturn(Collections.EMPTY_SET);
        expect(m_locationMonitorDao.getStatusChangesBetween(startDate, endDate)).andReturn(statusChanges);
        expect(m_locationMonitorDao.getAllStatusChangesAt(startDate)).andReturn(Collections.EMPTY_SET);


        replayEverything();
        SimpleWebTable table = m_service.createFacilityStatusTable(startDate,
                                                                   endDate);
        verifyEverything();
        
        //System.out.print(table.toString());
        
        SimpleWebTable expectedTable = new SimpleWebTable();
        expectedTable.setTitle("Distributed Poller Status Summary");
        expectedTable.addColumn("Area", "simpleWebTableRowLabel");
        expectedTable.addColumn("Location", "simpleWebTableRowLabel");
        expectedTable.addColumn("Application 1", "simpleWebTableRowLabel");
        expectedTable.addColumn("Application 2", "simpleWebTableRowLabel");

        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "simpleWebTableRowLabel");
        expectedTable.addCell("Raleigh", "simpleWebTableRowLabel");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusDetails.htm?location=Raleigh&application=Application+1");
        expectedTable.addCell("75.000%", "Normal", "distributedStatusDetails.htm?location=Raleigh&application=Application+2");
        
        expectedTable.newRow();
        expectedTable.addCell("OpenNMS NC", "simpleWebTableRowLabel");
        expectedTable.addCell("Durham", "simpleWebTableRowLabel");
        expectedTable.addCell("0.000%", "Normal", "distributedStatusDetails.htm?location=Durham&application=Application+1");
        expectedTable.addCell("0.000%", "Normal", "distributedStatusDetails.htm?location=Durham&application=Application+2");
        
        expectedTable.newRow();
        expectedTable.addCell("OpenNMS OH", "simpleWebTableRowLabel");
        expectedTable.addCell("Columbus", "simpleWebTableRowLabel");
        expectedTable.addCell("0.000%", "Indeterminate", "distributedStatusDetails.htm?location=Columbus&application=Application+1");
        expectedTable.addCell("0.000%", "Indeterminate", "distributedStatusDetails.htm?location=Columbus&application=Application+2");

        assertTableEquals(expectedTable, table);
    }
    
    public void testPercentageCalculation() {
        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        Collection<OnmsLocationSpecificStatus> statuses = new HashSet<OnmsLocationSpecificStatus>();
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061011-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061012-00:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpsService, PollStatus.available(), "20061012-06:00:00"));
        statuses.add(createStatus(m_locationMonitor1_1, httpService, PollStatus.available(), "20061013-00:00:00"));

        Date startDate = new Date(2006 - 1900, 10 - 1, 12, 0, 0, 0);
        Date endDate = new Date(2006 - 1900, 10 - 1, 13, 0, 0, 0);

//        expect(m_locationMonitorDao.getStatusChangesBetween(startDate, endDate)).andReturn(statuses);

        replayEverything();
        String percentage =
            m_service.calculatePercentageUptime(Collections.singleton(m_locationMonitor1_1),
                                                m_application1.getMemberServices(),
                                                statuses,
                                                startDate,
                                                endDate);
        verifyEverything();
        
        assertEquals("percentage", "75.000%", percentage);
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
                
                assertEquals(rowColumn + "content",
                             expectedColumn.getContent(),
                             column.getContent());
                assertEquals(rowColumn + "styleClass",
                             expectedColumn.getStyleClass(),
                             column.getStyleClass());
                assertEquals(rowColumn + "link",
                             expectedColumn.getLink(),
                             column.getLink());

            }
        }
    }

    public void resetEverything() {
        reset(m_applicationDao);
        reset(m_locationMonitorDao);
        reset(m_pollerConfig);
        reset(m_monitoredServiceDao);
    }

    public void expectEverything() {
        resetEverything();
        
        expect(m_applicationDao.findByName("Application 1")).andReturn(m_application1);
        expect(m_locationMonitorDao.findMonitoringLocationDefinition(m_locationDefinition1.getName())).andReturn(m_locationDefinition1);
        expect(m_locationMonitorDao.findByLocationDefinition(m_locationDefinition1)).andReturn(Collections.singleton(m_locationMonitor1_1));
        expect(m_pollerConfig.getPackage("raleigh")).andReturn(m_pkg);
        expect(m_pollerConfig.getServiceSelectorForPackage(m_pkg)).andReturn(m_selector);
        expect(m_monitoredServiceDao.findMatchingServices(m_selector)).andReturn(m_services);


        OnmsMonitoredService httpService = findMonitoredService(m_services, m_ip, "HTTP");
        OnmsMonitoredService httpsService = findMonitoredService(m_services, m_ip, "HTTPS");

        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpService)).andReturn(new OnmsLocationSpecificStatus(m_locationMonitor1_1, httpService, PollStatus.available()));
        expect(m_locationMonitorDao.getMostRecentStatusChange(m_locationMonitor1_1, httpsService)).andReturn(null);
    }

    public void replayEverything() {
        replay(m_pollerConfig);
        replay(m_monitoredServiceDao);
        replay(m_locationMonitorDao);
        replay(m_applicationDao);
    }

    public void verifyEverything() {
        verify(m_pollerConfig);
        verify(m_monitoredServiceDao);
        verify(m_locationMonitorDao);
        verify(m_applicationDao);
    }

    
    public OnmsMonitoredService findMonitoredService(Collection<OnmsMonitoredService> services, String interfaceIp, String serviceName) {
        for (OnmsMonitoredService service : services) {
            if (interfaceIp.equals(service.getIpAddress())
                    && serviceName.equals(service.getServiceName())) {
                return service;
            }
        }
        
        fail("Could not find service \"" + serviceName + "\" on interface \""
             + interfaceIp + "\"");
        
        // This will never be reached due to the above fail()
        return null;
    }

}

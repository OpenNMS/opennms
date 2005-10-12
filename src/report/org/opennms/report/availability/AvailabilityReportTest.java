package org.opennms.report.availability;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.lang.Math;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.mock.MockCategoryFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockUtil;

public class AvailabilityReportTest extends TestCase {
	
	protected MockDatabase m_db;
	protected Categories m_categories; 
	protected Calendar calendar;
	protected MockCategoryFactory m_catFactory;
	
	protected void setUp() throws Exception {
		super.setUp();
		Locale.setDefault(Locale.US);
		calendar = new GregorianCalendar();
		//date fror report run is 18th May 2005
		calendar.set(2005, 4, 18);
		MockLogAppender.setupLogging();
		m_categories = new Categories();
		m_db = new MockDatabase();
		DatabaseConnectionFactory.setInstance(m_db);
		m_catFactory = new MockCategoryFactory();
		CategoryFactory.setInstance(m_catFactory);
		m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (1,'test1.availability.opennms.org','2004-03-01 09:00:00','A')");
		m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (2,'test2.availability.opennms.org','2004-03-01 09:00:00','A')");
					
		m_db.update("insert into service (serviceid, servicename) values\n" +
					"(1, 'ICMP');");
		m_db.update("insert into service (serviceid, servicename) values\n" +	
					"(2, 'HTTP');");
		m_db.update("insert into service (serviceid, servicename) values\n" +	
					"(3, 'SNMP');");
		
		m_db.update("insert into ipinterface (nodeid, ipaddr, ismanaged) values\n" +
					"(1,'192.168.100.1','M');");
		m_db.update("insert into ipinterface (nodeid, ipaddr, ismanaged) values\n" +		
					"(2,'192.168.100.2','M');");
		m_db.update("insert into ipinterface (nodeid, ipaddr, ismanaged) values\n" +		
					"(2,'192.168.100.3','M');");
		
		m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status) values " +
				    "(1,'192.168.100.1',1,'A');");
		m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status) values " +
				    "(2,'192.168.100.2',1,'A');");
		/*m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status) values " +
					"(2,'192.168.100.2',2,'A');");*/
		m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status) values " +
					"(2,'192.168.100.3',1,'A');");
		
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
				    "(1,1,'192.168.100.1',1,'2005-05-01 09:00:00','2005-05-01 09:30:00');");
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
				    "(2,2,'192.168.100.2',1,'2005-05-01 10:00:00','2005-05-02 10:00:00');");
		
		// test data for LastMonthsDailyAvailability report
		
		// insert 30 minute outage on one node - 99.3056% availability
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
	    			"(3,1,'192.168.100.1',1,'2005-04-02 10:00:00','2005-04-02 10:30:00');");
		// insert 60 minute outage on one interface and 59 minute outages on another - 97.2454
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
		"(4,1,'192.168.100.1',1,'2005-04-03 11:30:00','2005-04-03 12:30:00');");
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
		"(5,2,'192.168.100.2',1,'2005-04-03 23:00:00','2005-04-03 23:59:00');");
		// test an outage that spans 60 minutes across midnight - 99.3056% on each day, well, not exactly
		// its 29 minutes 99.3059 on the fist day and 31 minutes 99.3052 on the second.
		m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values " +
		"(6,2,'192.168.100.3',1,'2005-04-04 23:30:00','2005-04-05 00:30:00');");
		
	}
	
	private Section getSectionByName (Category category, String sectionName) {
		
		Section match = null;
		
		CatSections[] catSections = category.getCatSections();
		for(int i= 0; i < catSections.length; i++) {
			Section[] section = catSections[i].getSection();
			for(int j= 0; j < section.length; j++) {
				if ( sectionName.equals(section[j].getSectionName()) ) {
					match = section[j];
				}
			}
		}			
		
		return match;
	}
	
	
	private Day getCalSectionDay(Category category, String title, int row, int col) {
		
		Section calSection = getSectionByName(category, title);
		CalendarTable table = calSection.getCalendarTable();
		Week week = table.getWeek(row);
		return week.getDay(col);
		
	}
	
	
	// helper method to round to 4 decimal places.
	
	private double fourDec(double number) {
		return (Math.round(number * 10000.0)) / 10000.0;
	}
	
	private int numRowsWithValue(Section section, String title, String data){
		
		int rowMatched = 0;
		boolean titlematch;
		boolean datamatch;
		
		ClassicTable table = section.getClassicTable();
		Rows rows = table.getRows();
		Row[] row = rows.getRow();
		for(int j= 0; j < row.length; j++) {
			Value[] value = row[j].getValue();
			titlematch = false;
			datamatch = false;
			for(int k= 0; k < value.length; k++){
				if (value[k].getType().equals("title") &&
						value[k].getContent().equals(title))
					titlematch = true;	
				if (value[k].getType().equals("data") &&
						value[k].getContent().equals(data))
					datamatch = true;
				if (datamatch && titlematch)
					rowMatched++;
			}	
		}			
		return rowMatched; 
	}
	
	private Report buildReport(Calendar calendar, String calFormat){
		
		
		Report report = new Report();
		report.setLogo("wherever");
        ViewInfo viewInfo = new ViewInfo();
        report.setViewInfo(viewInfo);
        report.setCategories(m_categories);
		AvailabilityData availData = null;
        try {
			availData = new AvailabilityData("Network Interfaces", report, "HTML", calFormat, calendar);
		} catch (MarshalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return report;
	}
	
	public void testMyDatabase () {
		assertEquals(2, m_db.countRows("select * from node"));
		assertEquals(3, m_db.countRows("select * from service"));
		assertEquals(3, m_db.countRows("select * from ipinterface"));	
		assertEquals(3, m_db.countRows("select * from ifservices"));
//		assertEquals(3, m_db.countRows("select * from outages"));
		assertEquals(1, m_db.countRows("select * from ipinterface where ipaddr = '192.168.100.1'"));
	}
	
	public void testBuiltClassicReport () {
		
		Report report = buildReport(calendar,"classic");
		Categories categories = report.getCategories();
		Category category = categories.getCategory(0);
		assertNotNull(report.getCategories());
		assertEquals(1,categories.getCategoryCount());
		
		// basic testst
		assertEquals(2,category.getNodeCount());
		assertEquals(3,category.getIpaddrCount());
		assertEquals(3,category.getServiceCount());
		
		Section section = getSectionByName(category,"LastMonthsDailyAvailability");
		assertNull(section.getCalendarTable());
					
		
	}
	public void testBuiltCalendarReport () {
								
		Calendar calendar = new GregorianCalendar(2005,4,20);
		long oneHundred = 100;
		Day day;
		Report report = buildReport(calendar,"calendar");
		Categories categories = report.getCategories();
		Category category = categories.getCategory(0);
		assertNotNull(report.getCategories());
		assertEquals(1,categories.getCategoryCount());
		
		assertEquals(2,category.getNodeCount());
		assertEquals(3,category.getIpaddrCount());
		assertEquals(3,category.getServiceCount());
		
		
		Section calSection = getSectionByName(category,"LastMonthsDailyAvailability");
		
		// First four days in month are invisible for US...
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,0);
		assertFalse(day.getVisible());
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,1);
		assertFalse(day.getVisible());
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,2);
		assertFalse(day.getVisible());
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,4);
		assertFalse(day.getVisible());
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,5);
		assertEquals(oneHundred, day.getPctValue(), 0);
		assertTrue(day.getVisible());
		assertEquals(1,day.getDate());
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,6);
		assertEquals(99.3056, fourDec(day.getPctValue()), 0);
		assertTrue(day.getVisible());
		assertEquals(2,day.getDate());
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,0);
		assertEquals(97.2454, fourDec(day.getPctValue()), 0);
		assertTrue(day.getVisible());
		assertEquals(3,day.getDate());
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,1);
		assertEquals(99.3059, fourDec(day.getPctValue()), 0);
		assertTrue(day.getVisible());
		assertEquals(4,day.getDate());
		
		day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,2);
		assertEquals(99.3052, fourDec(day.getPctValue()), 0);
		assertTrue(day.getVisible());
		assertEquals(5,day.getDate());
		
	}
	

	
		
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

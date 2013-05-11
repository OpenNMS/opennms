/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.availability;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import junit.framework.TestCase;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.mock.MockCategoryFactory;
import org.opennms.reporting.availability.svclayer.LegacyAvailabilityDataService;

public class AvailabilityCalculatorTest extends TestCase {

    protected MockDatabase m_db;

    protected Categories m_categories;

    protected Calendar m_calendar;

    protected MockCategoryFactory m_catFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Locale.setDefault(Locale.US);
        m_calendar = new GregorianCalendar();
        // date for report run is 18th May 2005
        m_calendar.set(2005, Calendar.MAY, 18);
        MockLogAppender.setupLogging();
        m_categories = new Categories();

        m_db = new MockDatabase();
        DataSourceFactory.setInstance(m_db);

        InputStream is = ConfigurationTestUtils.getInputStreamForConfigFile("database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(is));
        is.close();

        m_catFactory = new MockCategoryFactory();
        CategoryFactory.setInstance(m_catFactory);
        m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (1,'test1.availability.opennms.org','2004-03-01 09:00:00','A')");
        m_db.update("insert into node (nodeID, nodelabel, nodeCreateTime, nodeType) values (2,'test2.availability.opennms.org','2004-03-01 09:00:00','A')");

        m_db.update("insert into service (serviceid, servicename) values\n"
                + "(1, 'ICMP');");
        m_db.update("insert into service (serviceid, servicename) values\n"
                + "(2, 'HTTP');");
        m_db.update("insert into service (serviceid, servicename) values\n"
                + "(3, 'SNMP');");

        m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
                + "(1, 1,'192.168.100.1','M');");
        m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
                + "(2, 2,'192.168.100.2','M');");
        m_db.update("insert into ipinterface (id, nodeid, ipaddr, ismanaged) values\n"
                + "(3, 2,'192.168.100.3','M');");

        m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
                + "(1,'192.168.100.1',1,'A', 1);");
        m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
                + "(2,'192.168.100.2',1,'A', 2);");
        /*
         * m_db.update("insert into ifservices (nodeid, ipaddr, serviceid,
         * status, ipInterfaceId) values " + "(2,'192.168.100.2',2,'A', 2);");
         */
        m_db.update("insert into ifservices (nodeid, ipaddr, serviceid, status, ipInterfaceId) values "
                + "(2,'192.168.100.3',1,'A', 3);");

        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(1,1,'192.168.100.1',1,'2005-05-01 09:00:00','2005-05-01 09:30:00');");
        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(2,2,'192.168.100.2',1,'2005-05-01 10:00:00','2005-05-02 10:00:00');");

        // test data for LastMonthsDailyAvailability report

        // insert 30 minute outage on one node - 99.3056% availability
        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(3,1,'192.168.100.1',1,'2005-04-02 10:00:00','2005-04-02 10:30:00');");
        // insert 60 minute outage on one interface and 59 minute outages on
        // another - 97.2454
        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(4,1,'192.168.100.1',1,'2005-04-03 11:30:00','2005-04-03 12:30:00');");
        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(5,2,'192.168.100.2',1,'2005-04-03 23:00:00','2005-04-03 23:59:00');");
        // test an outage that spans 60 minutes across midnight - 99.3056% on
        // each day, well, not exactly
        // its 29 minutes 99.3059 on the fist day and 31 minutes 99.3052 on
        // the second.
        m_db.update("insert into outages (outageid, nodeid, ipaddr, serviceid, ifLostService, ifRegainedService) values "
                + "(6,2,'192.168.100.3',1,'2005-04-04 23:30:00','2005-04-05 00:30:00');");

    }

    private Section getSectionByName(Category category, String sectionName) {

        Section match = null;

        CatSections[] catSections = category.getCatSections();
        for (int i = 0; i < catSections.length; i++) {
            Section[] section = catSections[i].getSection();
            for (int j = 0; j < section.length; j++) {
                if (sectionName.equals(section[j].getSectionName())) {
                    match = section[j];
                }
            }
        }

        return match;
    }

    private Day getCalSectionDay(Category category, String title, int row,
            int col) {

        Section calSection = getSectionByName(category, title);
        CalendarTable table = calSection.getCalendarTable();
        Week week = table.getWeek(row);
        return week.getDay(col);

    }

    // helper method to round to 4 decimal places.

    private double fourDec(double number) {
        return (Math.round(number * 10000.0)) / 10000.0;
    }

    /*
    private int numRowsWithValue(Section section, String title, String data) {

        int rowMatched = 0;
        boolean titlematch;
        boolean datamatch;

        ClassicTable table = section.getClassicTable();
        Rows rows = table.getRows();
        Row[] row = rows.getRow();
        for (int j = 0; j < row.length; j++) {
            Value[] value = row[j].getValue();
            titlematch = false;
            datamatch = false;
            for (int k = 0; k < value.length; k++) {
                if (value[k].getType().equals("title")
                        && value[k].getContent().equals(title))
                    titlematch = true;
                if (value[k].getType().equals("data")
                        && value[k].getContent().equals(data))
                    datamatch = true;
                if (datamatch && titlematch)
                    rowMatched++;
            }
        }
        return rowMatched;
    }
    */

    private Report buildReport(Calendar calendar, String calFormat) {

        Report report = null;

        // Date periodEndDate = m_calendar.getTime();

        /*
         * report.setLogo("wherever"); ViewInfo viewInfo = new ViewInfo();
         * report.setViewInfo(viewInfo); report.setCategories(m_categories);
         */

        //AvailabilityData availData = null;
        try {
            AvailabilityCalculator calculator = new AvailabilityCalculatorImpl();
            AvailabilityData data = new AvailabilityData();
            data.setAvailabilityDataService(new LegacyAvailabilityDataService());
            calculator.setAvailabilityData(data);
            calculator.setPeriodEndDate(m_calendar.getTime());
            calculator.setLogoURL("wahtever");
            calculator.setCalendar(calendar);
            calculator.setReportFormat("PDF");
            calculator.setMonthFormat(calFormat);
            calculator.setCategoryName("Network Interfaces");
            calculator.calculate();
            report = calculator.getReport();
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
        return report;
    }

    public void testMyDatabase() {
        assertEquals("node DB count", 2, m_db.countRows("select * from node"));
        assertEquals("service DB count", 3,
                     m_db.countRows("select * from service"));
        assertEquals("ipinterface DB count", 3,
                     m_db.countRows("select * from ipinterface"));
        assertEquals("interface services DB count", 3,
                     m_db.countRows("select * from ifservices"));
        // assertEquals("outages DB count", 3, m_db.countRows("select * from
        // outages"));
        assertEquals(
                     "ip interface DB count where ipaddr = 192.168.100.1",
                     1,
                     m_db.countRows("select * from ipinterface where ipaddr = '192.168.100.1'"));
        assertEquals(
                     "number of interfaces returned from IPLIKE",
                     3,
                     m_db.countRows("select * from ipinterface where iplike(ipaddr,'192.168.100.*')"));
    }

    public void testBuiltClassicReport() {

        Report report = buildReport(m_calendar, "classic");

        assertNotNull("report", report);

        assertNotNull("report categories", report.getCategories());
        Categories categories = report.getCategories();

        assertEquals("category count", 1, categories.getCategoryCount());
        Category category = categories.getCategory(0);

        // basic testst
        assertEquals("category node count", 2, category.getNodeCount());
        assertEquals("category ip address count", 3,
                     category.getIpaddrCount());
        assertEquals("category service count", 3, category.getServiceCount());

        Section section = getSectionByName(category,
                                           "LastMonthsDailyAvailability");
        assertNull("section calendar table", section.getCalendarTable());
        Created created = report.getCreated();
        assertNotNull("report created period", created.getPeriod());

    }

    public void testBuiltCalendarReport() {

        Calendar calendar = new GregorianCalendar(2005, 4, 20);
        long oneHundred = 100;
        Day day;

        Report report = buildReport(calendar, "calendar");

        assertNotNull("report", report);

        assertNotNull("report categories", report.getCategories());
        Categories categories = report.getCategories();

        assertEquals("category count", 1, categories.getCategoryCount());
        Category category = categories.getCategory(0);

        assertEquals("category node count", 2, category.getNodeCount());
        assertEquals("category ip address count", 3,
                     category.getIpaddrCount());
        assertEquals("category service count", 3, category.getServiceCount());

        // Section calSection = getSectionByName(category, "LastMonthsDailyAvailability");

        // First four days in month are invisible for US...

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,0);
        assertNotNull("day 0,0 object", day);
        assertFalse("day 0,0 visibility", day.getVisible());
        
        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,1);
        assertNotNull("day 0,1 object", day);
        assertFalse("day 0,1 visibility", day.getVisible());
        
        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,2);
        assertNotNull("day 0,2 object", day);
        assertFalse("day 0,2 visibility", day.getVisible());
        
        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,4);
        assertNotNull("day 0,4 object", day);
        assertFalse("day 0,4 visibility", day.getVisible());

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,5);
        assertNotNull("day 0,5 object", day);
        assertEquals("day 0,5 percentage value", oneHundred, day.getPctValue(), 0);
        assertTrue("day 0,5 visibility", day.getVisible());
        assertEquals("day 0,5 date", 1,day.getDate());

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,6);
        assertNotNull("day 0,6 object", day);
        assertEquals("day 0,6 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
        assertTrue("day 0,6 visibility", day.getVisible());
        assertEquals("day 0,6 date", 2,day.getDate());

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,0);
        assertNotNull("day 1,0 object", day);
        assertEquals("day 1,0 percentage value", 97.2454, fourDec(day.getPctValue()), 0);
        assertTrue("day 1,0 visibility", day.getVisible());
        assertEquals("day 1,0 date", 3,day.getDate());

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,1);
        assertNotNull("day 1,1 object", day);
        assertEquals("day 1,1 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
        assertTrue("day 1,1 visibility", day.getVisible());
        assertEquals("day 1,1 date", 4,day.getDate());

        day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,2);
        assertNotNull("day 1,2 object", day);
        assertEquals("day 1,2 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
        assertTrue("day 1,2 visibility", day.getVisible());
        assertEquals("day 1,2 date", 5,day.getDate());
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.availability;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.annotation.Resource;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.mock.MockCategoryFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath*:/META-INF/opennms/component-reporting.xml",
        "classpath:/META-INF/opennms/applicationContext-availabilityDatabasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AvailabilityReportIntegrationTest implements InitializingBean {
    
    @Resource
    AvailabilityCalculator calendarAvailabilityCalculator;
    
    @Resource
    AvailabilityCalculator classicAvailabilityCalculator;
    
    @Autowired
    AvailabilityDatabasePopulator m_dbPopulator;

    protected Categories m_categories;

    protected Calendar m_calendar;

    protected MockCategoryFactory m_catFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {

        Locale.setDefault(Locale.US);
        m_calendar = new GregorianCalendar();
        // date fror report run is 18th May 2005
        m_calendar.set(2005, Calendar.MAY, 18);
        MockLogAppender.setupLogging();
        m_categories = new Categories();
        
        m_dbPopulator.populateDatabase();

        m_catFactory = new MockCategoryFactory();
        CategoryFactory.setInstance(m_catFactory);

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

    
    @Test
    @Ignore("Don't run this test (which has less coverage than the next) until I figure out why we're holding onto the previous test's dataSource.")
    public void testBuiltClassicReport() {
        
        try {
            classicAvailabilityCalculator.setPeriodEndDate(m_calendar.getTime());
            classicAvailabilityCalculator.setLogoURL("wahtever");
            classicAvailabilityCalculator.setReportFormat("PDF");
            classicAvailabilityCalculator.setMonthFormat("classic");
            classicAvailabilityCalculator.setCategoryName("Network Interfaces");

            classicAvailabilityCalculator.calculate();
            Report report = classicAvailabilityCalculator.getReport();
            
            Assert.assertNotNull("report", report);

            Assert.assertNotNull("report categories", report.getCategories());
            Categories categories = report.getCategories();

            Assert.assertEquals("category count", 1, categories.getCategoryCount());
            Category category = categories.getCategory(0);

            // basic test
            Assert.assertEquals("category node count", 2, category.getNodeCount());
            Assert.assertEquals("category ip address count", 3,
                         category.getIpaddrCount());
            Assert.assertEquals("category service count", 3, category.getServiceCount());

            Section section = getSectionByName(category,
                                               "LastMonthsDailyAvailability");
            Assert.assertNull("section calendar table", section.getCalendarTable());
            Created created = report.getCreated();
            Assert.assertNotNull("report created period", created.getPeriod());
            
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }

    }

    //TODO indigo: Spring Injection for DefaultRemoteRepositoryConfigDao necessary
    @Ignore
    @Test
    public void testBuiltCalendarReport() {

        long oneHundred = 100;
        Day day;
        
        try {
            calendarAvailabilityCalculator.setPeriodEndDate(m_calendar.getTime());
            calendarAvailabilityCalculator.setLogoURL("wahtever");
            calendarAvailabilityCalculator.setReportFormat("PDF");
            calendarAvailabilityCalculator.setMonthFormat("calendar");
            calendarAvailabilityCalculator.setCategoryName("Network Interfaces");

			calendarAvailabilityCalculator.calculate();
            Report report = calendarAvailabilityCalculator.getReport();
            
            Assert.assertNotNull("report", report);

            Assert.assertNotNull("report categories", report.getCategories());
            Categories categories = report.getCategories();

            Assert.assertEquals("category count", 1, categories.getCategoryCount());
            Category category = categories.getCategory(0);

            Assert.assertEquals("category node count", 2, category.getNodeCount());
            Assert.assertEquals("category ip address count", 3,
                         category.getIpaddrCount());
            Assert.assertEquals("category service count", 3, category.getServiceCount());

            // Section calSection = getSectionByName(category, "LastMonthsDailyAvailability");

            // First four days in month are invisible for US...

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,0);
            Assert.assertNotNull("day 0,0 object", day);
            Assert.assertFalse("day 0,0 visibility", day.getVisible());
            
            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,1);
            Assert.assertNotNull("day 0,1 object", day);
            Assert.assertFalse("day 0,1 visibility", day.getVisible());
            
            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,2);
            Assert.assertNotNull("day 0,2 object", day);
            Assert.assertFalse("day 0,2 visibility", day.getVisible());
            
            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,4);
            Assert.assertNotNull("day 0,4 object", day);
            Assert.assertFalse("day 0,4 visibility", day.getVisible());

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,5);
            Assert.assertNotNull("day 0,5 object", day);
            Assert.assertEquals("day 0,5 percentage value", oneHundred, day.getPctValue(), 0);
            Assert.assertTrue("day 0,5 visibility", day.getVisible());
            Assert.assertEquals("day 0,5 date", 1,day.getDate());

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",0,6);
            Assert.assertNotNull("day 0,6 object", day);
            Assert.assertEquals("day 0,6 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
            Assert.assertTrue("day 0,6 visibility", day.getVisible());
            Assert.assertEquals("day 0,6 date", 2,day.getDate());

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,0);
            Assert.assertNotNull("day 1,0 object", day);
            Assert.assertEquals("day 1,0 percentage value", 97.2454, fourDec(day.getPctValue()), 0);
            Assert.assertTrue("day 1,0 visibility", day.getVisible());
            Assert.assertEquals("day 1,0 date", 3,day.getDate());

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,1);
            Assert.assertNotNull("day 1,1 object", day);
            Assert.assertEquals("day 1,1 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
            Assert.assertTrue("day 1,1 visibility", day.getVisible());
            Assert.assertEquals("day 1,1 date", 4,day.getDate());

            day = getCalSectionDay(category,"LastMonthsDailyAvailability",1,2);
            Assert.assertNotNull("day 1,2 object", day);
            Assert.assertEquals("day 1,2 percentage value", 99.3056, fourDec(day.getPctValue()), 0);
            Assert.assertTrue("day 1,2 visibility", day.getVisible());
            Assert.assertEquals("day 1,2 date", 5,day.getDate());
            
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
  
    }

}

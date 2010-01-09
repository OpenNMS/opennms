/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 19, 2009 jonathan@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.integration.reporting.DeliveryOptions;
import org.opennms.api.integration.reporting.ReportService;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.test.mock.MockLogAppender;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.webflow.test.MockRequestContext;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:org/opennms/web/svclayer/schedulerServiceTest.xml"
})

/**
 * Unit tests for DefaultSchedulerService
 * 
 * @author <a href="mailto:jonathand@opennms.org">Jonathan Sartin</a>
 */

public class DefaultSchedulerServiceTest {
    
    @Autowired 
    private DefaultSchedulerService m_schedulerService;
    
    @Autowired
    private SchedulerFactoryBean m_schedulerFactory;
    
    @Autowired
    private JobDetailBean m_jobDetail;
 
    @Autowired
    private ReportService m_reportService;
    
    @Autowired
    private DatabaseReportConfigDao m_reportConfigDao;
    
    Scheduler m_scheduler;
    
    private static DatabaseReportCriteria m_criteria;
    private static String REPORT_ID = "test";
    private static String REPORT_SERVICE = "mockReportService";
    private static String CRON_EXPRESSION = "0 * * * * ?";
    private static DeliveryOptions m_deliveryOptions;
    private static final String TRIGGER_GROUP = "reporting";
    
    @BeforeClass
    public static void setUp() {
        MockLogAppender.setupLogging();
        m_deliveryOptions = new DeliveryOptions();
        m_criteria = new DatabaseReportCriteria();
        m_criteria.setReportId(REPORT_ID);
    }
    @Before
    public void resetReportService() {
        reset(m_reportService);
        reset(m_reportConfigDao);
        m_scheduler = (Scheduler) m_schedulerFactory.getScheduler();
        
    }
    
    @Test
    public void testWiring() {
        Assert.assertNotNull(m_schedulerService);
        Assert.assertNotNull(m_schedulerFactory);
        Assert.assertNotNull(m_jobDetail);
        Assert.assertNotNull(m_reportService);
        Assert.assertNotNull(m_reportConfigDao);
    }
    
    @Test
    public void testExecuteSuccess() throws InterruptedException {
        //
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        m_reportService.run(m_criteria.getReportParms(), m_deliveryOptions, REPORT_ID);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success",m_schedulerService.execute(m_criteria, m_deliveryOptions, context));
        //give the trigger a chance to fire
        Thread.sleep(1000);
        verify(m_reportService);
        verify(m_reportConfigDao);
    }
    
    @Test
    public void testExecuteFailure() throws InterruptedException {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(false);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("error",m_schedulerService.execute(m_criteria, m_deliveryOptions, context));
        // give the trigger a chance to fire
        Thread.sleep(1000);
        verify(m_reportService);
        verify(m_reportConfigDao);
    }

    @Test
    public void testScheduleBadCronExpression() {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("error", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "invalidTrigger", "bad expression", context));
        verify(m_reportService);
        verify(m_reportConfigDao);
    }
    
    @Test
    public void testScheduleAndRemove() throws SchedulerException {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "validTrigger", CRON_EXPRESSION, context));
        verify(m_reportService);
        verify(m_reportConfigDao);
        String[] triggers = m_scheduler.getTriggerNames(TRIGGER_GROUP);
        assertEquals(1,triggers.length);
        assertEquals("validTrigger",triggers[0]);
        m_schedulerService.removeTrigger("validTrigger");
        assertEquals(0,m_scheduler.getTriggerNames(TRIGGER_GROUP).length);
    }
    
    @Test
    public void testMultipleTriggers() throws SchedulerException {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true).times(2);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE).times(2);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        // this trigger fires every 10 minutes starting at 0 minutes past the hour
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "validTrigger", "0 0/10 * * * ?", context));
        // this trigger fires every 10 minutes starting at 5 minutes past the hour
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "secondValidTrigger", "0 5/10 * * * ?", context));
        verify(m_reportService);
        verify(m_reportConfigDao);
        String[] triggers = m_scheduler.getTriggerNames(TRIGGER_GROUP);
        assertEquals(2,triggers.length);
        assertEquals("validTrigger",triggers[0]);
        assertEquals("secondValidTrigger",triggers[1]);
        m_schedulerService.removeTrigger("validTrigger");
        m_schedulerService.removeTrigger("secondValidTrigger");
        assertEquals(0,m_scheduler.getTriggerNames(TRIGGER_GROUP).length);
    }
    
    @Test
    public void testScheduleAndRun() throws SchedulerException, InterruptedException {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        m_reportService.run(m_criteria.getReportParms(), m_deliveryOptions, REPORT_ID);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "validTrigger", CRON_EXPRESSION, context));
        // give the trigger a chance to fire (one minute)
        Thread.sleep(61000);
        m_schedulerService.removeTrigger("validTrigger");
        verify(m_reportService);
        verify(m_reportConfigDao);
        m_schedulerService.removeTrigger("validTrigger");
        assertEquals(0,m_scheduler.getTriggerNames(TRIGGER_GROUP).length);  
    }
    
    @Test
    public void testExists() {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "validTrigger", CRON_EXPRESSION, context));
        verify(m_reportService);
        verify(m_reportConfigDao);
        assertTrue(m_schedulerService.exists("validTrigger"));
        assertFalse(m_schedulerService.exists("bogusTrigger"));
        m_schedulerService.removeTrigger("validTrigger");
        
    }
    
    @Test
    public void testGetTriggerDescriptions() {
        expect(m_reportService.validate(m_criteria.getReportParms(), REPORT_ID)).andReturn(true);
        replay(m_reportService);
        expect(m_reportConfigDao.getReportService(REPORT_ID)).andReturn(REPORT_SERVICE);
        replay(m_reportConfigDao);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success", m_schedulerService.addCronTrigger(m_criteria, m_deliveryOptions, "validTrigger", CRON_EXPRESSION, context));
        verify(m_reportService);
        verify(m_reportConfigDao);
        assertEquals(1,m_schedulerService.getTriggerDescriptions().size());
        assertEquals("validTrigger",m_schedulerService.getTriggerDescriptions().get(0).getTriggerName());
        m_schedulerService.removeTrigger("validTrigger");
    }
}

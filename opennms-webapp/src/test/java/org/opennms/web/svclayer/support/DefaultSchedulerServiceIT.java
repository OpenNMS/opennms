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

package org.opennms.web.svclayer.support;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Unit tests for DefaultSchedulerService
 * 
 * @author <a href="mailto:jonathand@opennms.org">Jonathan Sartin</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:org/opennms/web/svclayer/schedulerServiceTest.xml"
})
public class DefaultSchedulerServiceIT implements InitializingBean {
    
    @Autowired 
    private DefaultSchedulerService m_schedulerService;
    
    @Autowired
    private SchedulerFactoryBean m_schedulerFactory;
    
    @Autowired
    private ReportWrapperService m_reportWrapperService;
    
    Scheduler m_scheduler;
    
    private static ReportParameters m_criteria;
    private static String REPORT_ID = "test";
    private static String CRON_EXPRESSION = "0 * * * * ?";
    private static final String TRIGGER_GROUP = "reporting";
    
    @BeforeClass
    public static void setUp() {
        MockLogAppender.setupLogging();
        m_criteria = new ReportParameters();
        m_criteria.setReportId(REPORT_ID);
    }
    @Before
    public void resetReportService() {
        reset(m_reportWrapperService);
        m_scheduler = (Scheduler) m_schedulerFactory.getScheduler();
        
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    public void testExecuteSuccess() throws InterruptedException {
        //
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testExecuteSuccessTrigger");
        m_reportWrapperService.run(m_criteria, ReportMode.IMMEDIATE, deliveryOptions, REPORT_ID);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success",m_schedulerService.execute(REPORT_ID, m_criteria, deliveryOptions, context));
        //give the trigger a chance to fire
        Thread.sleep(1000);
        verify(m_reportWrapperService);
    }
    
    @Test
    public void testExecuteFailure() throws InterruptedException {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(false);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testExecuteFailureTrigger");
        assertEquals("error",m_schedulerService.execute(REPORT_ID, m_criteria, deliveryOptions, context));
        // give the trigger a chance to fire
        Thread.sleep(1000);
        verify(m_reportWrapperService);
    }

    @Test
    public void testScheduleBadCronExpression() {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testScheduleBadCronExpressionTrigger");
        assertEquals("error", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions, "bad expression", context));
        verify(m_reportWrapperService);
    }
    
    @Test
    public void testScheduleAndRemove() throws SchedulerException {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testScheduleAndRemoveTrigger");
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions, CRON_EXPRESSION, context));
        verify(m_reportWrapperService);
        Set<TriggerKey> triggers = m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(TRIGGER_GROUP));
        assertEquals(1, triggers.size());
        assertEquals("testScheduleAndRemoveTrigger",triggers.iterator().next().getName());
        m_schedulerService.removeTrigger("testScheduleAndRemoveTrigger");
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(TRIGGER_GROUP)).size());
    }
    
    @Test
    public void testMultipleTriggers() throws SchedulerException {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true).times(2);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        // this trigger fires every 10 minutes starting at 0 minutes past the hour
        DeliveryOptions deliveryOptions1 = new DeliveryOptions();
        deliveryOptions1.setInstanceId("trigger1");
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions1, "0 0/10 * * * ?", context));
        // this trigger fires every 10 minutes starting at 5 minutes past the hour
        DeliveryOptions deliveryOptions2 = new DeliveryOptions();
        deliveryOptions2.setInstanceId("trigger2");
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions2, "0 5/10 * * * ?", context));
        verify(m_reportWrapperService);
        final List<String> triggers = m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(TRIGGER_GROUP)).stream().map(TriggerKey::getName).collect(Collectors.toList());
        assertEquals(2,triggers.size());
        assertTrue(triggers.contains("trigger1"));
        assertTrue(triggers.contains("trigger2"));
        m_schedulerService.removeTrigger("trigger1");
        m_schedulerService.removeTrigger("trigger2");
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(TRIGGER_GROUP)).size());
    }
    
    @Test
    public void testScheduleAndRun() throws SchedulerException, InterruptedException {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testScheduleAndRunTrigger");
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        m_reportWrapperService.run(m_criteria, ReportMode.SCHEDULED, deliveryOptions, REPORT_ID);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions, CRON_EXPRESSION, context));
        // give the trigger a chance to fire (one minute)
        Thread.sleep(61000);
        m_schedulerService.removeTrigger("testScheduleAndRunTrigger");
        verify(m_reportWrapperService);
        m_schedulerService.removeTrigger("testScheduleAndRunTrigger");
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(TRIGGER_GROUP)).size());
    }
    
    @Test
    public void testExists() {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testExistsTrigger");
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions, CRON_EXPRESSION, context));
        verify(m_reportWrapperService);
        assertTrue(m_schedulerService.exists("testExistsTrigger"));
        assertFalse(m_schedulerService.exists("bogusTrigger"));
        m_schedulerService.removeTrigger("testExistsTrigger");
        
    }
    
    @Test
    public void testGetTriggerDescriptions() {
        expect(m_reportWrapperService.validate(m_criteria, REPORT_ID)).andReturn(true);
        replay(m_reportWrapperService);
        MockRequestContext context = new MockRequestContext();
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setInstanceId("testGetTriggerDescriptionsTrigger");
        assertEquals("success", m_schedulerService.addCronTrigger(REPORT_ID, m_criteria, deliveryOptions, CRON_EXPRESSION, context));
        verify(m_reportWrapperService);
        assertEquals(1,m_schedulerService.getTriggerDescriptions().size());
        assertEquals("testGetTriggerDescriptionsTrigger",m_schedulerService.getTriggerDescriptions().get(0).getTriggerName());
        m_schedulerService.removeTrigger("testGetTriggerDescriptionsTrigger");
    }
}

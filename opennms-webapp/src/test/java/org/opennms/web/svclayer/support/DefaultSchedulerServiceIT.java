/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.awaitility.core.ConditionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.DeliveryConfig;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.reporting.core.svclayer.ScheduleConfig;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;

/**
 * Unit tests for DefaultSchedulerService
 *
 * @author <a href="mailto:jonathand@opennms.org">Jonathan Sartin</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/org/opennms/web/svclayer/schedulerServiceTest.xml"
})
public class DefaultSchedulerServiceIT {

    @Autowired
    private DefaultSchedulerService m_schedulerService;

    @Autowired
    private SchedulerFactoryBean m_schedulerFactory;

    @Autowired
    private ReportWrapperService m_reportWrapperService;

    private Scheduler m_scheduler;

    private static String REPORT_ID = "test";
    private static String CRON_EXPRESSION = "0/10 * * * * ?";
    private static final String TRIGGER_GROUP = "reporting";
    private static final String INSTANCE_ID = DefaultSchedulerServiceIT.class.getSimpleName();

    private ReportParameters reportParameters;
    private DeliveryOptions deliveryOptions;
    private JobExecutionVerificationListener jobListener;
    private ConditionFactory await;

    @BeforeClass
    public static void setUp() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void resetReportService() throws SchedulerException {
        BeanUtils.assertAutowiring(this);

        m_scheduler = m_schedulerFactory.getScheduler();

        reportParameters = new ReportParameters();
        reportParameters.setReportId(REPORT_ID);

        deliveryOptions = new DeliveryOptions();
        deliveryOptions.setFormat(ReportFormat.PDF);
        deliveryOptions.setPersist(true);
        deliveryOptions.setInstanceId(INSTANCE_ID);

        jobListener = new JobExecutionVerificationListener();
        m_scheduler.getListenerManager().addJobListener(jobListener);

        await = await().atMost(10, SECONDS).pollInterval(250, MILLISECONDS);

        m_schedulerService.getTriggerDescriptions().forEach(trigger -> m_schedulerService.removeTrigger(trigger.getTriggerName()));
        assertThat(m_schedulerService.getTriggerDescriptions(), hasSize(0));
    }

    @Test
    public void testExecuteSuccess() {
        m_reportWrapperService.run(reportParameters, ReportMode.IMMEDIATE, deliveryOptions, REPORT_ID);
        m_schedulerService.execute(new DeliveryConfig(reportParameters, deliveryOptions));
        await.until(() -> jobListener.wasSuccess());
    }

    @Test
    public void testEitherSendMailPersistOrWebhookMustBeSet() {
        deliveryOptions.setPersist(false);

        try {
            m_schedulerService.execute(new DeliveryConfig(reportParameters, deliveryOptions));
            fail("Expected exception, but wasn't thrown");
        } catch (org.opennms.web.svclayer.support.SchedulerContextException ex) {
            assertThat(ex.getContext(), is("sendMail_persist_webhook"));
        }
    }

    @Test
    public void testExecuteFailure() {
        final ReportFloatParm intParm = new ReportFloatParm();
        intParm.setName("floatParm1");
        intParm.setValue(null);

        final ReportParameters parameters = new ReportParameters();
        parameters.setReportId(REPORT_ID);
        parameters.setFloatParms(Lists.newArrayList(intParm));

        try {
            m_schedulerService.execute(new DeliveryConfig(parameters /* should fail due to missing value */, deliveryOptions));
            fail("Expected exception, but wasn't thrown");
        } catch (org.opennms.web.svclayer.support.SchedulerContextException ex) {
            assertThat(ex.getContext(), is("floatParm1"));
        }

        assertThat(jobListener.actualExecutions, is(0));
    }

    @Test
    public void testScheduleBadCronExpression() {
        try {
            m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions, "bad expression"));
            fail("Expected exception, but wasn't thrown");
        } catch (SchedulerContextException ex) {
            assertThat(ex.getContext(), is("cronExpression"));
        }
        assertThat(jobListener.actualExecutions, is(0));
    }

    @Test
    public void testScheduleAndRemove() throws SchedulerException {
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions, CRON_EXPRESSION));
        Set<TriggerKey> triggers = m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(TRIGGER_GROUP));
        assertEquals(1, triggers.size());
        assertEquals(INSTANCE_ID,triggers.iterator().next().getName());
        m_schedulerService.removeTrigger(INSTANCE_ID);
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(TRIGGER_GROUP)).size());
    }

    @Test
    public void testMultipleTriggers() throws SchedulerException {
        // this trigger fires every 10 minutes starting at 0 minutes past the hour
        DeliveryOptions deliveryOptions1 = new DeliveryOptions();
        deliveryOptions1.setFormat(ReportFormat.PDF);
        deliveryOptions1.setPersist(true);
        deliveryOptions1.setInstanceId("trigger1");
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions1, "0 0/10 * * * ?"));

        // this trigger fires every 10 minutes starting at 5 minutes past the hour
        DeliveryOptions deliveryOptions2 = new DeliveryOptions();
        deliveryOptions2.setFormat(ReportFormat.PDF);
        deliveryOptions2.setInstanceId("trigger2");
        deliveryOptions2.setPersist(true);
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions2, "0 5/10 * * * ?"));

        final List<String> triggers = m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(TRIGGER_GROUP)).stream().map(TriggerKey::getName).collect(Collectors.toList());
        assertEquals(2,triggers.size());
        assertThat(triggers.contains("trigger1"), is(true));
        assertThat(triggers.contains("trigger2"), is(true));
        m_schedulerService.removeTrigger("trigger1");
        m_schedulerService.removeTrigger("trigger2");
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(TRIGGER_GROUP)).size());
    }

    @Test
    public void testScheduleAndRun() throws SchedulerException {
        // Run, Schedule and Verify
        m_reportWrapperService.run(reportParameters, ReportMode.SCHEDULED, deliveryOptions, REPORT_ID);
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions, CRON_EXPRESSION));
        await.until(() -> jobListener.wasSuccess());

        // Remove Trigger and Verify
        m_schedulerService.removeTrigger(INSTANCE_ID);
        m_schedulerService.removeTrigger(INSTANCE_ID);
        assertEquals(0, m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(TRIGGER_GROUP)).size());
    }

    @Test
    public void testExists() {
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions, CRON_EXPRESSION));
        assertThat(m_schedulerService.exists(INSTANCE_ID), is(true));
        assertThat(m_schedulerService.exists("bogusTrigger"), is(false));
        m_schedulerService.removeTrigger(INSTANCE_ID);
    }

    @Test
    public void testGetTriggerDescriptions() {
        m_schedulerService.addCronTrigger(new ScheduleConfig(reportParameters, deliveryOptions, CRON_EXPRESSION));
        assertThat(m_schedulerService.getTriggerDescriptions(), hasSize(1));
        assertThat(m_schedulerService.getTriggerDescriptions().get(0).getTriggerName(), is(INSTANCE_ID));
        m_schedulerService.removeTrigger(INSTANCE_ID);
    }

    private class JobExecutionVerificationListener implements JobListener {

        private int expectedExecutions = 1;
        private JobExecutionException exception;
        private int actualExecutions;

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext context) {

        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext context) {

        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            this.exception = jobException;
            this.actualExecutions++;
        }

        public boolean wasSuccess() {
            return exception == null && actualExecutions == expectedExecutions;
        }

    }
}

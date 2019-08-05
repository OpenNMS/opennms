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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.DeliveryConfig;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.opennms.reporting.core.svclayer.ScheduleConfig;
import org.opennms.web.svclayer.SchedulerService;
import org.opennms.web.svclayer.model.TriggerDescription;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>DefaultSchedulerService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultSchedulerService implements InitializingBean, SchedulerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSchedulerService.class);

    private Scheduler m_scheduler;
    private JobDetail m_jobDetail;
    private String m_triggerGroup;
    private ReportWrapperService m_reportWrapperService;

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.debug("Adding job {} to scheduler", m_jobDetail.getKey().getName());
        m_scheduler.addJob(m_jobDetail, true);
    }

    @Override
    public List<TriggerDescription> getTriggerDescriptions() {
        final List<TriggerDescription> triggerDescriptions = new ArrayList<>();
        try {
            final Set<TriggerKey> triggerKeys = m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(m_triggerGroup));
            for (TriggerKey triggerKey : triggerKeys) {
                TriggerDescription description = new TriggerDescription();
                Trigger trigger = m_scheduler.getTrigger(triggerKey);
                description.setNextFireTime(trigger.getNextFireTime());
                description.setTriggerName(triggerKey.getName());
                description.setReportId((String)trigger.getJobDataMap().get("reportId"));
                description.setDeliveryOptions((DeliveryOptions) trigger.getJobDataMap().get("deliveryOptions"));
                description.setReportParameters(((ReportParameters) trigger.getJobDataMap().get("criteria")));
                if (trigger instanceof CronTriggerImpl) {
                    description.setCronExpression(((CronTriggerImpl)trigger).getCronExpression());
                }
                triggerDescriptions.add(description);
            }
        } catch (SchedulerException e) {
            LOG.error("exception retrieving trigger descriptions", e);
            throw new RuntimeException(e); // TODO MVR
        }

        return triggerDescriptions;

    }

    @Override
    public Boolean exists(String triggerName) {
        try {
            final Trigger trigger = m_scheduler.getTrigger(new TriggerKey(triggerName, m_triggerGroup));
            if (trigger != null) {
                return true;
            }
        } catch (SchedulerException e) {
            LOG.error("exception looking up trigger name: {}", triggerName, e);
            throw new RuntimeException(e); // TODO MVR
        }
        return false;
    }

    @Override
    public void removeTrigger(String triggerName) {
        try {
            m_scheduler.unscheduleJob(new TriggerKey(triggerName, m_triggerGroup));
        } catch (SchedulerException e) {
            LOG.error("exception when attempting to remove trigger {}", triggerName, e);
            throw new RuntimeException(e); // TODO MVR
        }

    }

    @Override
    public void removeTriggers(String[] triggerNames) {
        for (String triggerName : triggerNames) {
            removeTrigger(triggerName);
        }
    }

    @Override
    public void updateCronTrigger(String cronTrigger, ScheduleConfig scheduleConfig) {
        Objects.requireNonNull(cronTrigger);
        Objects.requireNonNull(scheduleConfig);
        validate(scheduleConfig);

        final TriggerKey triggerKey = new TriggerKey(cronTrigger, m_triggerGroup);
        final ReportParameters parameters = scheduleConfig.getReportParameters();
        final DeliveryOptions deliveryOptions = scheduleConfig.getDeliveryOptions();
        final String cronExpression = scheduleConfig.getCronExpression();
        try {
            final Trigger trigger = m_scheduler.getTrigger(triggerKey);
            trigger.getJobDataMap().put("criteria", parameters);
            trigger.getJobDataMap().put("deliveryOptions", deliveryOptions);
            trigger.getJobDataMap().put("cronExpression", cronExpression);
            ((CronTriggerImpl) trigger).setCronExpression(cronExpression);
            m_scheduler.rescheduleJob(triggerKey, trigger);
        } catch(SchedulerException e) {
            LOG.error("TODO MVR", e);
            throw new org.opennms.web.svclayer.support.SchedulerException(e);
        } catch (ParseException e) {
            LOG.error("Provided cron expression '{}' could not be parsed", cronExpression, e);
            throw new org.opennms.web.svclayer.support.InvalidCronExpressionException(e, cronExpression);
        }
    }

    private void validate(DeliveryConfig deliveryConfig) {
        Objects.requireNonNull(deliveryConfig);

        if (!m_reportWrapperService.validate(deliveryConfig.getReportParameters(), deliveryConfig.getReportId())) {
            throw new org.opennms.web.svclayer.support.SchedulerException("An error occurred when validating the report parameters");
        }
    }

    @Override
    public void addCronTrigger(ScheduleConfig scheduleConfig) {
        Objects.requireNonNull(scheduleConfig);
        validate(scheduleConfig);

        final CronTriggerImpl cronTrigger = new CronTriggerImpl();
        final ReportParameters parameters = scheduleConfig.getReportParameters();
        final DeliveryOptions deliveryOptions = scheduleConfig.getDeliveryOptions();
        final String cronExpression = scheduleConfig.getCronExpression();
        try {
            cronTrigger.setGroup(m_triggerGroup);
            cronTrigger.setName(deliveryOptions.getInstanceId());
            cronTrigger.setJobName(m_jobDetail.getKey().getName());
            cronTrigger.setCronExpression(cronExpression);
        } catch (ParseException e) {
            LOG.error("Provided cron expression '{}' could not be parsed", cronExpression, e);
            throw new InvalidCronExpressionException(e, cronExpression);
        }
        cronTrigger.setJobName(m_jobDetail.getKey().getName());
        cronTrigger.getJobDataMap().put("criteria", parameters);
        cronTrigger.getJobDataMap().put("reportId", parameters.getReportId());
        cronTrigger.getJobDataMap().put("deliveryOptions", deliveryOptions);
        cronTrigger.getJobDataMap().put("mode", ReportMode.SCHEDULED);

        try {
            m_scheduler.scheduleJob(cronTrigger);
        } catch (SchedulerException e) {
            throw new org.opennms.web.svclayer.support.SchedulerException(e);
        }
    }

    @Override
    public void execute(DeliveryConfig deliveryConfig) {
        Objects.requireNonNull(deliveryConfig);
        validate(deliveryConfig);

        final ReportParameters parameters = deliveryConfig.getReportParameters();
        final DeliveryOptions deliveryOptions = deliveryConfig.getDeliveryOptions();
        final SimpleTriggerImpl trigger = new SimpleTriggerImpl(deliveryOptions.getInstanceId(), m_triggerGroup, new Date(), null, 0, 0L);
        trigger.setJobName(m_jobDetail.getKey().getName());
        trigger.getJobDataMap().put("criteria", parameters);
        trigger.getJobDataMap().put("reportId", parameters.getReportId());
        trigger.getJobDataMap().put("deliveryOptions", deliveryOptions);
        trigger.getJobDataMap().put("mode", ReportMode.IMMEDIATE);
        try {
            m_scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw new org.opennms.web.svclayer.support.SchedulerException(e);
        }
    }

    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public void setJobDetail(JobDetail reportJob) {
        m_jobDetail = reportJob;
    }

    public void setTriggerGroup(String triggerGroup) {
        m_triggerGroup = triggerGroup;
    }

    public void setReportWrapperService(ReportWrapperService reportWrapperService) {
        m_reportWrapperService = reportWrapperService;
    }
}

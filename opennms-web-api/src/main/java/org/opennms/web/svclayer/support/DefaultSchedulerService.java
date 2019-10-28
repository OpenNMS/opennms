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

import static org.opennms.api.reporting.ReportParameterBuilder.Intervals;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportParmVisitor;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.api.reporting.parameter.ReportTimezoneParm;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.DeliveryConfig;
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

import com.google.common.base.Strings;

/**
 * <p>DefaultSchedulerService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultSchedulerService implements InitializingBean, SchedulerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSchedulerService.class);
    private static final String PROVIDE_A_VALUE_TEXT = "Please provide a value";
    private static final String PROVIDED_VALUE_GREATER_ZERO_TEXT = "The provided value must be > 0";

    private Scheduler m_scheduler;
    private JobDetail m_jobDetail;
    private String m_triggerGroup;

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
            throw new org.opennms.web.svclayer.support.SchedulerException("Could not retrieve triggers: " +  e.getMessage(), e);
        }

        return triggerDescriptions;

    }

    @Override
    public Boolean exists(String triggerName) {
        Objects.requireNonNull(triggerName);
        try {
            final Trigger trigger = m_scheduler.getTrigger(new TriggerKey(triggerName, m_triggerGroup));
            if (trigger != null) {
                return true;
            }
        } catch (SchedulerException e) {
            LOG.error("exception looking up trigger name: {}", triggerName, e);
            throw new org.opennms.web.svclayer.support.SchedulerException("Could not retrieve trigger '" + triggerName + " ': " +  e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void removeTrigger(String triggerName) {
        try {
            m_scheduler.unscheduleJob(new TriggerKey(triggerName, m_triggerGroup));
        } catch (SchedulerException e) {
            LOG.error("exception when attempting to remove trigger {}", triggerName, e);
            throw new org.opennms.web.svclayer.support.SchedulerException("Could not remove trigger '" + triggerName + " ': " +  e.getMessage(), e);
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
        validate(scheduleConfig, ReportMode.SCHEDULED, true);

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
            LOG.error("Could not update cron trigger {}:{}", cronTrigger, e.getMessage(), e);
            throw new org.opennms.web.svclayer.support.SchedulerException("An unexpected error occurred while updating cron trigger " + cronTrigger, e);
        } catch (ParseException e) {
            LOG.error("Provided cron expression '{}' could not be parsed", cronExpression, e);
            throw new org.opennms.web.svclayer.support.InvalidCronExpressionException(e, cronExpression);
        }
    }

    @Override
    public void addCronTrigger(ScheduleConfig scheduleConfig) {
        Objects.requireNonNull(scheduleConfig);
        validate(scheduleConfig, ReportMode.SCHEDULED, false);

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
        validate(deliveryConfig, ReportMode.IMMEDIATE, false);

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

    private void validate(DeliveryConfig deliveryConfig, ReportMode reportMode, boolean update) {
        Objects.requireNonNull(deliveryConfig);
        Objects.requireNonNull(reportMode);
        validate(deliveryConfig.getReportParameters(), reportMode);
        validate(deliveryConfig.getDeliveryOptions(), update);
    }

    private void validate(DeliveryOptions deliveryOptions, boolean update) {
        Objects.requireNonNull(deliveryOptions);

        try {
            if (!update) { // We skip the instanceId check if we update
                final Set<String> intanceIds = m_scheduler.getTriggerKeys(GroupMatcher.groupEquals(m_triggerGroup)).stream()
                        .map(tk -> tk.getName())
                        .collect(Collectors.toSet());
                if (intanceIds.contains(deliveryOptions.getInstanceId())) {
                    throw new SchedulerContextException("instanceId", "The provided value already exists");
                }
                if (Strings.isNullOrEmpty(deliveryOptions.getInstanceId())) {
                    throw new SchedulerContextException("instanceId", PROVIDE_A_VALUE_TEXT);
                }
            }

            if (!deliveryOptions.isSendMail() && !deliveryOptions.isPersist() && !deliveryOptions.isWebhook()) {
                throw new SchedulerContextException("sendMail_persist_webhook", "Either sendMail, webhook or persist must be set");
            }
            if (deliveryOptions.getFormat() == null) {
                throw new SchedulerContextException("format", PROVIDE_A_VALUE_TEXT);
            }
            if (deliveryOptions.isSendMail()) {
                if (Strings.isNullOrEmpty(deliveryOptions.getMailTo())) {
                    throw new SchedulerContextException("mailTo", PROVIDE_A_VALUE_TEXT);
                }
                // Try parsing the input
                try {
                    InternetAddress.parse(deliveryOptions.getMailTo(), false);
                } catch (AddressException e) {
                    throw new SchedulerContextException("mailTo", "Provided recipients could not be parsed: {0}", e.getMessage(), e);
                }
            }
            if (deliveryOptions.isWebhook()) {
                if (Strings.isNullOrEmpty(deliveryOptions.getWebhookUrl())) {
                    throw new SchedulerContextException("webhookUrl", PROVIDE_A_VALUE_TEXT);
                }
                try {
                    new URL(deliveryOptions.getWebhookUrl());
                } catch (MalformedURLException ex) {
                    throw new SchedulerContextException("webhookUrl", "The provided URL ''{0}'' is not valid: ''{1}''", deliveryOptions.getWebhookUrl(), ex.getMessage());
                }
            }
        } catch (SchedulerException e) {
            throw new org.opennms.web.svclayer.support.SchedulerException(e);
        }
    }

    private void validate(ReportParameters reportParameters, ReportMode reportMode) {
        Objects.requireNonNull(reportParameters);
        Objects.requireNonNull(reportMode);
        final ReportParmVisitor validator = new ParameterRequiredVisitor(reportMode);
        if (reportParameters.getStringParms() != null) {
            for (ReportStringParm eachParm : reportParameters.getStringParms()) {
                validator.visit(eachParm);
            }
        }
        if (reportParameters.getIntParms() != null) {
            for (ReportIntParm eachParm : reportParameters.getIntParms()) {
                validator.visit(eachParm);
            }
        }
        if (reportParameters.getFloatParms() != null) {
            for (ReportFloatParm eachParm : reportParameters.getFloatParms()) {
                validator.visit(eachParm);
            }
        }
        if (reportParameters.getDoubleParms() != null) {
            for (ReportDoubleParm eachParm : reportParameters.getDoubleParms()) {
                validator.visit(eachParm);
            }
        }
        if (reportParameters.getDateParms() != null) {
            for (ReportDateParm eachParm : reportParameters.getDateParms()) {
                validator.visit(eachParm);
            }
        }
    }

    /**
     * This visitor enforces that each value is actually set, as it is required by default.
     */
    private static class ParameterRequiredVisitor implements ReportParmVisitor {

        private final ReportMode mode;

        public ParameterRequiredVisitor(ReportMode reportMode) {
            this.mode = Objects.requireNonNull(reportMode);
        }

        @Override
        public void visit(ReportStringParm parm) {
            if (Strings.isNullOrEmpty(parm.getValue())) {
                throw new SchedulerContextException(parm.getName(), PROVIDE_A_VALUE_TEXT);
            }
        }

        @Override
        public void visit(ReportIntParm parm) {

        }

        @Override
        public void visit(ReportFloatParm parm) {
            if (parm.getValue() == null) {
                throw new SchedulerContextException(parm.getName(), PROVIDE_A_VALUE_TEXT);
            }
        }

        @Override
        public void visit(ReportDoubleParm parm) {
            if (parm.getValue() == null) {
                throw new SchedulerContextException(parm.getName(), PROVIDE_A_VALUE_TEXT);
            }
        }

        @Override
        public void visit(ReportTimezoneParm parm) {
            if (parm.getValue() == null) {
                throw new SchedulerContextException(parm.getName(), PROVIDE_A_VALUE_TEXT);
            }
        }

        @Override
        public void visit(ReportDateParm parm) {
            if (parm.getUseAbsoluteDate() || mode == ReportMode.IMMEDIATE) {
                if (parm.getDate() == null){
                    throw new SchedulerContextException(parm.getName() + "Date", PROVIDE_A_VALUE_TEXT);
                }
            } else {
                if (parm.getInterval() == null || !Intervals.ALL.contains(parm.getInterval())) {
                    throw new SchedulerContextException(parm.getName() + "Interval", "The provided value must be any of the following {0}", Intervals.ALL);
                }
                if (parm.getCount() == null) {
                    throw new SchedulerContextException(parm.getName() + "Count", PROVIDED_VALUE_GREATER_ZERO_TEXT);
                }
            }
            if (parm.getHours() == null) {
                throw new SchedulerContextException(parm.getName() + "Hours", PROVIDE_A_VALUE_TEXT);
            }
            if (parm.getHours() < 0 || parm.getHours() > 23) {
                throw new SchedulerContextException(parm.getName() + "Hours", "Please provide a value between 0 and 23");
            }
            if (parm.getMinutes() == null) {
                throw new SchedulerContextException(parm.getName() + "Minutes", PROVIDE_A_VALUE_TEXT);
            }
            if (parm.getMinutes() < 0 || parm.getMinutes() > 59) {
                throw new SchedulerContextException(parm.getName() + "Minutes", "Please provide a value between 0 and 59");
            }
        }
    }
}

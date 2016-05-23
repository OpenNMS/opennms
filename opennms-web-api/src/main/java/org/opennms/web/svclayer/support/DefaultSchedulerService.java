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
import java.util.Set;

import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.reporting.core.svclayer.ReportServiceLocatorException;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
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
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.execution.RequestContext;

/**
 * <p>DefaultSchedulerService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultSchedulerService implements InitializingBean, SchedulerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSchedulerService.class);


    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String PARAMETER_ERROR = "Report parameters did not match the definition for the report please contact your OpenNMS administrator";
    private static final String SCHEDULER_ERROR = "An exception occurred when scheduling the report";
    private static final String TRIGGER_PARSE_ERROR = "An error occurred parsing the cron expression. It was not possible to schedule the report";
    private static final String REPORTID_ERROR = "An error occurred locating the report service bean";
    
    private Scheduler m_scheduler;
    private JobDetail m_jobDetail;
    private String m_triggerGroup;
    private ReportWrapperService m_reportWrapperService;

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        LOG.debug("Adding job {} to scheduler", m_jobDetail.getKey().getName());
        m_scheduler.addJob(m_jobDetail, true);

    }

    /**
     * <p>getTriggerDescriptions</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<TriggerDescription> getTriggerDescriptions() {

        List<TriggerDescription> triggerDescriptions = new ArrayList<TriggerDescription>();

        try {
            Set<TriggerKey> triggerKeys = m_scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(m_triggerGroup));
            for (TriggerKey triggerKey : triggerKeys) {
                TriggerDescription description = new TriggerDescription();
                Trigger trigger = m_scheduler.getTrigger(triggerKey);
                description.setNextFireTime(trigger.getNextFireTime());
                description.setTriggerName(triggerKey.getName());
                description.setReportId((String)trigger.getJobDataMap().get("reportId"));
                description.setDeliveryOptions((DeliveryOptions) trigger.getJobDataMap().get("deliveryOptions"));
                description.setReportParameters(((ReportParameters) trigger.getJobDataMap().get("criteria")).getReportParms());
                triggerDescriptions.add(description);

            }
        } catch (SchedulerException e) {
            LOG.error("exception lretrieving trigger descriptions", e);
        }

        return triggerDescriptions;

    }

    /** {@inheritDoc} */
    @Override
    public Boolean exists(String triggerName) {

        Boolean found = false;

        try {
            Trigger trigger = m_scheduler.getTrigger(new TriggerKey(triggerName,
                                                     m_triggerGroup));
            if (trigger != null) {
                found = true;
            }
        } catch (SchedulerException e) {
            LOG.error("exception looking up trigger name: {}", triggerName);
            LOG.error(e.getMessage());
        }

        return found;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.web.svclayer.support.SchedulerService#removeTrigger(java
     * .lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void removeTrigger(String triggerName) {
        try {
            m_scheduler.unscheduleJob(new TriggerKey(triggerName, m_triggerGroup));
        } catch (SchedulerException e) {
            LOG.error("exception when attempting to remove trigger {}", triggerName, e);
        }

    }

    /**
     * <p>removeTriggers</p>
     *
     * @param triggerNames an array of {@link java.lang.String} objects.
     */
    @Override
    public void removeTriggers(String[] triggerNames) {
        for (String triggerName : triggerNames) {
            removeTrigger(triggerName);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.web.svclayer.support.SchedulerService#addCronTrigger(org
     * .opennms.web.report.database.model.DatabaseReportCriteria,
     * java.lang.String, java.lang.String, java.lang.String,
     * org.springframework.webflow.execution.RequestContext)
     */
    /** {@inheritDoc} */
    @Override
    public String addCronTrigger(String id, ReportParameters criteria,
            DeliveryOptions deliveryOptions,
            String cronExpression, RequestContext context) {

        CronTriggerImpl cronTrigger = null;
        
        try {            
            if (m_reportWrapperService.validate(criteria,id) == false ) {
                LOG.error(PARAMETER_ERROR);
                context.getMessageContext().addMessage(
                                                       new MessageBuilder().error().defaultText(
                                                                                                PARAMETER_ERROR).build());
                return ERROR;
            } else {
                try {
                    cronTrigger = new CronTriggerImpl();
                    cronTrigger.setGroup(m_triggerGroup);
                    cronTrigger.setName(deliveryOptions.getInstanceId());
                    cronTrigger.setJobName(m_jobDetail.getKey().getName());
                    cronTrigger.setCronExpression(cronExpression);
                    // cronTrigger = new CronTrigger(triggerName, m_triggerGroup,
                    // cronExpression);
                } catch (ParseException e) {
                    LOG.error(TRIGGER_PARSE_ERROR, e);
                    context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(TRIGGER_PARSE_ERROR).build());
                    context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(e.getMessage()).build());
                    return ERROR;
                }

                cronTrigger.setJobName(m_jobDetail.getKey().getName());
                cronTrigger.getJobDataMap().put("criteria", (ReportParameters) criteria);
                cronTrigger.getJobDataMap().put("reportId", id);
                cronTrigger.getJobDataMap().put("mode", ReportMode.SCHEDULED);
                cronTrigger.getJobDataMap().put("deliveryOptions",
                                                (DeliveryOptions) deliveryOptions);
                try {
                    m_scheduler.scheduleJob(cronTrigger);
                } catch (SchedulerException e) {
                    LOG.error(SCHEDULER_ERROR, e);
                    context.getMessageContext().addMessage(
                                                           new MessageBuilder().error().defaultText(
                                                                                                    SCHEDULER_ERROR).build());
                    return ERROR;
                }

                return SUCCESS;
            }
        } catch (ReportServiceLocatorException e) {
            LOG.error(REPORTID_ERROR);
            context.getMessageContext().addMessage(
                                                   new MessageBuilder().error().defaultText(
                                                                                            REPORTID_ERROR).build());
            return ERROR;
        }

        
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.web.svclayer.support.SchedulerService#execute(org.opennms
     * .web.report.database.model.DatabaseReportCriteria, java.lang.String,
     * org.springframework.webflow.execution.RequestContext)
     */
    /** {@inheritDoc} */
    @Override
    public String execute(String id, ReportParameters criteria,
            DeliveryOptions deliveryOptions, RequestContext context) {

        try {
            if (m_reportWrapperService.validate(criteria,id) == false ) {
                context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(PARAMETER_ERROR).build());
                return ERROR;
            } else {
                SimpleTriggerImpl trigger = new SimpleTriggerImpl(deliveryOptions.getInstanceId(), m_triggerGroup, new Date(), null, 0, 0L);
                trigger.setJobName(m_jobDetail.getKey().getName());
                trigger.getJobDataMap().put("criteria", (ReportParameters) criteria);
                trigger.getJobDataMap().put("reportId", id);
                trigger.getJobDataMap().put("mode", ReportMode.IMMEDIATE);
                trigger.getJobDataMap().put("deliveryOptions", (DeliveryOptions) deliveryOptions);
                try {
                    m_scheduler.scheduleJob(trigger);
                } catch (SchedulerException e) {
                    LOG.warn(SCHEDULER_ERROR, e);
                    context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(SCHEDULER_ERROR).build());
                    return ERROR;
                }

                return SUCCESS;
            }
        } catch (ReportServiceLocatorException e) {
            LOG.error(REPORTID_ERROR, e);
            context.getMessageContext().addMessage(new MessageBuilder().error().defaultText(REPORTID_ERROR).build());
            return ERROR;
        }


    }

    

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.quartz.Scheduler} object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>setJobDetail</p>
     *
     * @param reportJob a {@link org.quartz.JobDetail} object.
     */
    public void setJobDetail(JobDetail reportJob) {
        m_jobDetail = reportJob;
    }

    /**
     * <p>setTriggerGroup</p>
     *
     * @param triggerGroup a {@link java.lang.String} object.
     */
    public void setTriggerGroup(String triggerGroup) {
        m_triggerGroup = triggerGroup;
    }

    /**
     * <p>setReportWrapperService</p>
     *
     * @param reportWrapperService a {@link org.opennms.reporting.core.svclayer.ReportWrapperService} object.
     */
    public void setReportWrapperService(ReportWrapperService reportWrapperService) {
        m_reportWrapperService = reportWrapperService;
    }


}

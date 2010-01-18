//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: December 15th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
package org.opennms.web.svclayer.support;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.api.reporting.DeliveryOptions;
import org.opennms.api.reporting.ReportService;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportServiceLocatorException;
import org.opennms.web.svclayer.SchedulerService;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.execution.RequestContext;

public class DefaultSchedulerService implements InitializingBean, SchedulerService {

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String PARAMETER_ERROR = "Report parameters did not match the definition for the report please contact your OpenNMS administrator";
    private static final String SCHEDULER_ERROR = "An exception occurred when scheduling the report";
    private static final String TRIGGER_PARSE_ERROR = "An error occurred parsing the cron expression. It was not possible to schedule the report";
    private static final String REPORTID_ERROR = "An error occurred locating the report service bean";
    
    private Scheduler m_scheduler;
    private JobDetail m_jobDetail;
    private DatabaseReportConfigDao m_configDao;
    private String m_triggerGroup;
    private ReportServiceLocator m_reportServiceLocator;

    public void afterPropertiesSet() throws Exception {

        log().debug("Adding job " + m_jobDetail.getName() + " to scheduler");
        m_scheduler.addJob(m_jobDetail, true);

    }

    public List<TriggerDescription> getTriggerDescriptions() {

        List<TriggerDescription> triggerDescriptions = new ArrayList<TriggerDescription>();

        try {
            String[] triggerNames = m_scheduler.getTriggerNames(m_triggerGroup);
            for (int j = 0; j < triggerNames.length; j++) {
                TriggerDescription description = new TriggerDescription();
                description.setNextFireTime(m_scheduler.getTrigger(
                                                                   triggerNames[j],
                                                                   m_triggerGroup).getNextFireTime());
                description.setTriggerName(triggerNames[j]);
                triggerDescriptions.add(description);

            }
        } catch (SchedulerException e) {
            log().error("exception looking retrieving trigger descriptions",
                        e);
        }

        return triggerDescriptions;

    }

    public Boolean exists(String triggerName) {

        Boolean found = false;

        try {
            Trigger trigger = m_scheduler.getTrigger(triggerName,
                                                     m_triggerGroup);
            if (trigger != null) {
                found = true;
            }
        } catch (SchedulerException e) {
            log().error("exception looking up trigger name: " + triggerName);
            log().error(e);
        }

        return found;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.opennms.web.svclayer.support.SchedulerService#removeTrigger(java
     * .lang.String)
     */
    public void removeTrigger(String triggerName) {
        try {
            m_scheduler.unscheduleJob(triggerName, m_triggerGroup);
        } catch (SchedulerException e) {
            log().error(
                        "exception when attempting to remove trigger "
                                + triggerName);
            log().error(e);
        }

    }

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
    public String addCronTrigger(DatabaseReportCriteria criteria,
            DeliveryOptions deliveryOptions, String triggerName,
            String cronExpression, RequestContext context) {

        CronTrigger cronTrigger = null;

        String reportServiceName = m_configDao.getReportService(criteria.getReportId());
        
        try {
            
            ReportService reportService = m_reportServiceLocator.getReportService(reportServiceName);
            
            if (reportService.validate(criteria.getReportParms(),
                                       criteria.getReportId()) == false) {
                log().error(PARAMETER_ERROR);
                context.getMessageContext().addMessage(
                                                       new MessageBuilder().error().defaultText(
                                                                                                PARAMETER_ERROR).build());
                return ERROR;
            } else {
                try {
                    cronTrigger = new CronTrigger();
                    cronTrigger.setGroup(m_triggerGroup);
                    cronTrigger.setName(triggerName);
                    cronTrigger.setJobName(m_jobDetail.getName());
                    cronTrigger.setCronExpression(cronExpression);
                    // cronTrigger = new CronTrigger(triggerName, m_triggerGroup,
                    // cronExpression);
                } catch (ParseException e) {
                    log().error(TRIGGER_PARSE_ERROR, e);
                    context.getMessageContext().addMessage(
                                                           new MessageBuilder().error().defaultText(
                                                                                                    TRIGGER_PARSE_ERROR).build());
                    return ERROR;
                }

                cronTrigger.setJobName(m_jobDetail.getName());
                cronTrigger.getJobDataMap().put("criteria", criteria);
                cronTrigger.getJobDataMap().put("deliveryOptions",
                                                deliveryOptions);
                cronTrigger.getJobDataMap().put("reportServiceName", reportServiceName);
                try {
                    m_scheduler.scheduleJob(cronTrigger);
                } catch (SchedulerException e) {
                    log().error(SCHEDULER_ERROR, e);
                    context.getMessageContext().addMessage(
                                                           new MessageBuilder().error().defaultText(
                                                                                                    SCHEDULER_ERROR).build());
                    return ERROR;
                }

                return SUCCESS;
            }
        } catch (ReportServiceLocatorException e) {
            log().error(REPORTID_ERROR);
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
    public String execute(DatabaseReportCriteria criteria,
            DeliveryOptions deliveryOptions, RequestContext context) {

        String reportServiceName = m_configDao.getReportService(criteria.getReportId());
        ReportService reportService;
        try {
            reportService = m_reportServiceLocator.getReportService(reportServiceName);
            if (reportService.validate(criteria.getReportParms(),
                                       criteria.getReportId()) == false) {
                context.getMessageContext().addMessage(
                                                       new MessageBuilder().error().defaultText(
                                                                                                PARAMETER_ERROR).build());
                return ERROR;
            } else {
                SimpleTrigger trigger = new SimpleTrigger("immediateTrigger",
                                                          m_triggerGroup,
                                                          new Date(), null, 0, 0L);
                trigger.setJobName(m_jobDetail.getName());
                trigger.getJobDataMap().put("criteria", criteria);
                trigger.getJobDataMap().put("deliveryOptions", deliveryOptions);
                trigger.getJobDataMap().put("reportServiceName", reportServiceName);
                try {
                    m_scheduler.scheduleJob(trigger);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                    context.getMessageContext().addMessage(
                                                           new MessageBuilder().error().defaultText(
                                                                                                    SCHEDULER_ERROR).build());
                    return ERROR;
                }

                return SUCCESS;
            }
        } catch (ReportServiceLocatorException e) {
            log().error(REPORTID_ERROR);
            context.getMessageContext().addMessage(
                                                   new MessageBuilder().error().defaultText(
                                                                                            REPORTID_ERROR).build());
            return ERROR;
        }


    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public void setJobDetail(JobDetail reportJob) {
        m_jobDetail = reportJob;
    }

    public void setDatabaseReportConfigDao(DatabaseReportConfigDao configDao) {
        m_configDao = configDao;
    }

    public void setTriggerGroup(String triggerGroup) {
        m_triggerGroup = triggerGroup;
    }

    public void setReportServiceLocator(ReportServiceLocator reportServiceLocator) {
        m_reportServiceLocator = reportServiceLocator;
    }


}

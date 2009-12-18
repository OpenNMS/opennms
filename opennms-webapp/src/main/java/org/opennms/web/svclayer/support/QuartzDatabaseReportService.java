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

import java.util.Date;

import org.opennms.api.integration.reporting.BatchDeliveryOptions;
import org.opennms.api.integration.reporting.BatchReportService;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.web.report.database.model.DatabaseReportCriteria;
import org.opennms.web.svclayer.DatabaseReportService;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.webflow.execution.RequestContext;

public class QuartzDatabaseReportService implements DatabaseReportService, ApplicationContextAware {

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String PARAMETER_ERROR = 
        "Report parameters did not match the definition for the report please contact your OpenNMS administrator";
    private static final String SCHEDULER_ERROR = 
        "An exception occurred when scheduling the report";
    
    private JobDetail m_jobDetail;
    private Scheduler m_scheduler;
    private ApplicationContext m_context;

    /* (non-Javadoc)
     * @see org.opennms.web.svclayer.DatabaseReportService#execute(org.opennms.web.svclayer.support.DatabaseReportCriteria)
     */
    
    public String execute(DatabaseReportCriteria criteria, RequestContext context) {
        
        String reportServiceName = (String)context.getFlowScope().get("reportServiceName");
        
        BatchReportService reportService = (BatchReportService)m_context.getBean(reportServiceName);
        
        if (reportService.validate(criteria.getReportParms(), criteria.getReportId()) == false) {
            context.getMessageContext().addMessage(new MessageBuilder().error()
                                                   .defaultText(PARAMETER_ERROR).build());
            return ERROR;
        } else {
            SimpleTrigger trigger = new SimpleTrigger("immediateTrigger",
                                                      null,
                                                      new Date(),
                                                      null,
                                                      0,
                                                      0L);
            trigger.getJobDataMap().put("criteria", criteria);
            trigger.getJobDataMap().put("reportServiceName", reportServiceName);
            try {
                m_scheduler.scheduleJob(m_jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
                context.getMessageContext().addMessage(new MessageBuilder().error()
                                                       .defaultText(SCHEDULER_ERROR).build());
                return ERROR;
            }

            return SUCCESS;
        }
    }
    
    public String execute(DatabaseReportCriteria criteria, BatchDeliveryOptions deliveryOptions, RequestContext context) {
        
        String reportServiceName = (String)context.getFlowScope().get("reportServiceName");
        
        BatchReportService reportService = (BatchReportService)m_context.getBean(reportServiceName);
        
        if (reportService.validate(criteria.getReportParms(), criteria.getReportId()) == false) {
            context.getMessageContext().addMessage(new MessageBuilder().error()
                                                   .defaultText(PARAMETER_ERROR).build());
            return ERROR;
        } else {
            SimpleTrigger trigger = new SimpleTrigger("immediateTrigger",
                                                      null,
                                                      new Date(),
                                                      null,
                                                      0,
                                                      0L);
            trigger.getJobDataMap().put("criteria", criteria);
            trigger.getJobDataMap().put("deliveryOptions", deliveryOptions);
            trigger.getJobDataMap().put("reportServiceName", reportServiceName);
            try {
                m_scheduler.scheduleJob(m_jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
                context.getMessageContext().addMessage(new MessageBuilder().error()
                                                       .defaultText(SCHEDULER_ERROR).build());
                return ERROR;
            }

            return SUCCESS;
        }
    }

    public void setJobDetail(JobDetail reportJob) {
        m_jobDetail = reportJob;
    }    
    
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        m_context = applicationContext;
    }

}

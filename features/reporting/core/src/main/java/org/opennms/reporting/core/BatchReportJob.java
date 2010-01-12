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
 * Created: November 17, 2009 jonathan@opennms.org
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

package org.opennms.reporting.core;

import org.opennms.api.integration.reporting.DeliveryOptions;
import org.opennms.api.integration.reporting.ReportService;
import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class BatchReportJob extends QuartzJobBean {
    
    private ApplicationContext m_context;

    @Override
    protected void executeInternal(JobExecutionContext jobContext)
            throws JobExecutionException {
        
        JobDataMap dataMap = jobContext.getMergedJobDataMap();
        DatabaseReportCriteria criteria = (DatabaseReportCriteria) dataMap.get("criteria");
        DeliveryOptions deliveryOptions = (DeliveryOptions) dataMap.get("deliveryOptions");
        
        // TODO this needs the reportServiceName in the criteria 
        ReportServiceLocator reportServiceLocator =
            (ReportServiceLocator)m_context.getBean("reportServiceLocator");
        
        ReportService reportService = reportServiceLocator.getReportService((String)dataMap.get("reportServiceName"));
        
        reportService.run(criteria.getReportParms(), deliveryOptions, criteria.getReportId());
        
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        m_context = applicationContext;
    }
    
    

}

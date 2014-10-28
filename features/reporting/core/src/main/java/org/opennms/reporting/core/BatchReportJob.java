/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.reporting.core;

import org.opennms.api.reporting.ReportMode;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.reporting.core.svclayer.ReportWrapperService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * <p>BatchReportJob class.</p>
 */
public class BatchReportJob extends QuartzJobBean {
    
    private ApplicationContext m_context;

    /** {@inheritDoc} */
    @Override
    protected void executeInternal(JobExecutionContext jobContext)
            throws JobExecutionException {
        
        JobDataMap dataMap = jobContext.getMergedJobDataMap();
       
        // TODO this needs the reportServiceName in the criteria 
        
//        
//        ReportServiceLocator reportServiceLocator =
//            (ReportServiceLocator)m_context.getBean("reportServiceLocator");
//        
//        ReportService reportService = reportServiceLocator.getReportService((String)dataMap.get("reportServiceName"));
//        
//        reportService.run(criteria.getReportParms(), 
//                          deliveryOptions, 
//                          (String)dataMap.get("reportId"));
        
        ReportWrapperService reportWrapperService = 
            (ReportWrapperService)m_context.getBean("reportWrapperService");
        
        reportWrapperService.run((ReportParameters) dataMap.get("criteria"),
                                 (ReportMode) dataMap.get("mode"),
                                 (DeliveryOptions) dataMap.get("deliveryOptions"),
                                 (String)dataMap.get("reportId"));
        
    }
    
    /**
     * <p>setApplicationContext</p>
     *
     * @param applicationContext a {@link org.springframework.context.ApplicationContext} object.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        m_context = applicationContext;
    }
    
    

}

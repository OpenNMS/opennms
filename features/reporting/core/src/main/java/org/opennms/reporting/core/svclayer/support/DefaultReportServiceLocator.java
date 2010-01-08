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
 * Created: January 7th 2010 Jonathan Sartin <jonathan@opennms.org>
 * 
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.reporting.core.svclayer.support;

import org.opennms.api.integration.reporting.ReportService;
import org.opennms.reporting.core.svclayer.ReportServiceLocator;
import org.opennms.reporting.core.svclayer.ReportServiceLocatorException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultReportServiceLocator implements ApplicationContextAware, ReportServiceLocator {

    private ApplicationContext m_applicationContext;

    public ReportService getReportService(String reportServiceName) throws ReportServiceLocatorException {
        
        ReportService reportService = (ReportService) m_applicationContext.getBean(reportServiceName);
        
        if (reportService == null) {
            throw new ReportServiceLocatorException("cannot locate report service bean: " + reportServiceName );       
        } else {
            return reportService;
        }

    }

    public void setApplicationContext(ApplicationContext applicationContext) {
            m_applicationContext = applicationContext;
    }

}

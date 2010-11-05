/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 29, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.dao.ReportdConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for ReportdConfigurationDao entity
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("reports")
@Transactional
public class ReportdRestService extends OnmsRestService {
    
    @Autowired
    ReportdConfigurationDao m_reportdDao;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ReportList getReports() {
        ReportList reportList = new ReportList(m_reportdDao.getReports());
        reportList.setTotalCount(reportList.size());
        return reportList;
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{reportName}")
    public Report getReport(@PathParam("reportName") String reportName) {
        return m_reportdDao.getReport(reportName);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addReport(Report report) {
        log().debug("addReport: Adding report " + report);
        m_reportdDao.getConfig().addReport(report);
        
        return Response.ok(report).build();
    }
    
    @DELETE
    @Path("{reportName}")
    public Response deleteReport(@PathParam("reportName") String reportName) {
        Report report = m_reportdDao.getReport(reportName);
        if(report == null) {
            throwException(Status.BAD_REQUEST, "deleteReport: Can't find report " + reportName);
        }
        
        log().debug("deleteReport: deleting report " + reportName);
        
        if(!m_reportdDao.deleteReport(reportName)) {
            throwException(Status.BAD_REQUEST, "deleteReport: Can't delete report " + reportName);
        }
        
        return Response.ok().build();
    }
    
}

package org.opennms.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.resource.PerRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.opennms.netmgt.dao.ReportdConfigurationDao;
import org.opennms.netmgt.config.reportd.ReportdConfiguration;
import org.opennms.netmgt.config.reportd.Report;
import org.opennms.netmgt.config.reportd.Parameter;

import org.opennms.web.reportd.ReportSchedule;
import org.opennms.web.reportd.ReportParameterWrapper;

@Component
@PerRequest
@Scope("prototype")
@Path("reporting")
@Transactional
public class ReportingRestService extends OnmsRestService {

    @Autowired
    private ReportdConfigurationDao m_reportdConfigurationDao;

    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("schedules")
    public List<ReportSchedule> getReports(){
        
        List<ReportSchedule> reports = new ArrayList<ReportSchedule>();
        for(Report r : m_reportdConfigurationDao.getReports()){
            
            reports.add(new ReportSchedule(r));
            
        }
        
        return reports;
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("schedules/{reportName}")
    public ReportSchedule getReport(@PathParam("reportName")String reportName){
        return new ReportSchedule(m_reportdConfigurationDao.getReport(reportName));
    }
    
    
    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("schedules/{reportName}")
    public Response deleteReport(@PathParam("reportName")String reportName){
        Response response;
        if(m_reportdConfigurationDao.deleteReport(reportName)){
            response = Response.ok().build();
        }
        else {
            response = Response.notModified().build();
        }
        
        return response;    
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("schedules{reportName}/recipients")
    public String[] getRecipients(@PathParam("reportName")String reportName){
        return m_reportdConfigurationDao.getReport(reportName).getRecipient();
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("schedules/{reportName}/parameters")
    public List<Parameter> getParameters(@PathParam("reportName")String reportName){
        return m_reportdConfigurationDao.getReport(reportName).getParameterCollection();
    }
    
    
}

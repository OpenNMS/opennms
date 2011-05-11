package org.opennms.isoc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.opennms.isoc.rest.example.JobInfoBean;
import org.opennms.isoc.rest.example.StatusInfoBean;

@Path("/status")
public class StatusBeanResource {
    
    StatusInfoBean statusInfoBean = new StatusInfoBean();
    
    {{
        statusInfoBean.jobs.add(new JobInfoBean("sample.doc", "printing...", 13));
    }}
    
    @GET
    @Produces("application/json")
    public StatusInfoBean getStatus() {
        return statusInfoBean;
    }
    
    @PUT
    @Consumes("application/json")
    public synchronized void setStatus(StatusInfoBean status) {
        this.statusInfoBean = status;
    }
}

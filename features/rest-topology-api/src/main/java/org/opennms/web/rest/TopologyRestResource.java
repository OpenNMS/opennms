package org.opennms.web.rest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@Scope("prototype")
@Path("topology")
public class TopologyRestResource {

    public class TopologyResponse{

    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public TopologyResponse getTopology(){
        return new TopologyResponse();
    }
}

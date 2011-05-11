package org.opennms.isoc.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/goodbyeworld")
public class GoodbyeWorldResource {

    
    @GET
    @Produces("text/plain")
    public String getClichedMessage() {
        return "Goodbye World";
    }
}

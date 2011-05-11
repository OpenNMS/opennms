package org.opennms.isoc.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/helloworld")
public class HelloWorldResource {

    @GET
    @Produces("application/json")
    public String getClichedMessage() {
        return "Hello World";
    }
}

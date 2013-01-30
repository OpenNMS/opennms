package org.opennms.features.rest.addon;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/later")
public class LaterResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayIt() {
		return "Bis bald! \t" + hashCode();
	}
}

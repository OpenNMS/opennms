package org.opennms.features.mibcompiler.rest.api;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.features.mibcompiler.rest.api.model.CompileMibRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/mib-compiler")
public interface MibCompilerRestService {


    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    Response uploadMibFiles(@Multipart("upload") List<Attachment> attachments,
                            @Context SecurityContext securityContext) throws Exception;

    @POST
    @Path("/compile-mib")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces("application/json")
    Response compilePendingMib(CompileMibRequest compileMibRequest, @Context SecurityContext securityContext) throws Exception;
}

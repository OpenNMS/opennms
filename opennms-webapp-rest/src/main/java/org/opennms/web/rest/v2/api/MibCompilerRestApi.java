package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("mib-compiler")
@Tag(name = "Mib-compiler", description = "Mib-compiler API")
public interface MibCompilerRestApi {

    @POST
    @Path("/files/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Response uploadMibFiles(@Multipart("upload") List<Attachment> attachments,
                            @Context SecurityContext securityContext) throws Exception;
}

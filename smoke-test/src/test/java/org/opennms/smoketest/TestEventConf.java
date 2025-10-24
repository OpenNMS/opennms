package org.opennms.smoketest;

import org.apache.camel.Attachment;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TestEventConf {

    public static void main (String [] args) throws IOException {

        String resourcePath = "/EVENT-CONF/3Com.events.xml";
        File tempFile = copyResourceToTempFile(resourcePath);

        // Upload
        TestEventConf test = new TestEventConf();
        Response response = test.uploadEventConfFile(tempFile);

        System.out.println("Response code: " + response.getStatus());
        System.out.println("Response body: " + response.readEntity(String.class));

    }


    public Response uploadEventConfFile(File file) {
        // Point to your REST API endpoint, e.g. /api/v2/eventconf/upload
        final WebTarget target = getTarget().path("upload");

        // Create multipart body
        try (FormDataMultiPart multipart = new FormDataMultiPart()) {
            FileDataBodyPart filePart = new FileDataBodyPart("upload", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            multipart.bodyPart(filePart);

            Invocation.Builder builder = getBuilder(target)
                    .header("Accept", MediaType.APPLICATION_JSON);

            return builder.post(Entity.entity(multipart, multipart.getMediaType()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private WebTarget getTarget() {
        final Client client = ClientBuilder.newBuilder()
                .register(org.glassfish.jersey.media.multipart.MultiPartFeature.class) // important
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .build();

        return client.target("http://localhost:8980/opennms/api/v2/eventconf");
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        String   authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes());
        return target.request().header("Authorization", authorizationHeader);
    }

    private static File copyResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream is = TestEventConf.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            File tempFile = File.createTempFile("eventconf-", ".xml");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }
            return tempFile;
        }
    }
}

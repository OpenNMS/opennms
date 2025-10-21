package org.opennms.web.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.controller.admin.thresholds.ThresholdController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EventConfApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThresholdController.class);

    private static final String EVENT_CONFIG_UPLOAD_URL = "/api/v2/eventconf/upload";
    private static final String EVENT_SOURCE_NAME = "programmatic.events";
    private static final String COOKIE_NAME = "JSESSIONID";

    private final ObjectMapper objectMapper = new ObjectMapper();


    public boolean uploadFileToApi(final Events events, final HttpServletRequest request) {
        LOGGER.info("Uploading event source file to database...");

        final byte[] eventData = marshalToXmlBytes(events);
        final String uploadUrl = getBaseUrl(request) + EVENT_CONFIG_UPLOAD_URL;

        final HttpPost uploadRequest = new HttpPost(uploadUrl);
        uploadRequest.setHeader("Cookie", getCookie(request));

        final HttpEntity multipart = MultipartEntityBuilder.create()
                .addBinaryBody("upload", eventData, ContentType.APPLICATION_XML, "programmatic.events.xml")
                .build();

        uploadRequest.setEntity(multipart);

        return executeHttpRequest(uploadRequest, "upload event file");
    }

    public Long getEventConfSourceIdByName(final HttpServletRequest request) {
        final String url = getBaseUrl(request) + "/api/v2/eventconf/sources/" + EVENT_SOURCE_NAME;

        final HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Cookie", getCookie(request));
        httpGet.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpGet)) {

            final int statusCode = response.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == HttpStatus.SC_OK) {
                final JsonNode jsonNode = objectMapper.readTree(responseBody);
                final long sourceId = jsonNode.path("sourceId").asLong();

                if (sourceId == 0) {
                    LOGGER.warn("No valid sourceId found in response for '{}'", EVENT_SOURCE_NAME);
                    return null;
                }

                LOGGER.debug("Fetched sourceId={} for '{}'", sourceId, EVENT_SOURCE_NAME);
                return sourceId;

            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                LOGGER.warn("EventConf source '{}' not found.", EVENT_SOURCE_NAME);
            } else {
                LOGGER.error("Failed to fetch sourceId. HTTP {}: {}", statusCode, responseBody);
            }

            return null;

        } catch (IOException e) {
            LOGGER.error("I/O error while fetching sourceId for '{}'", EVENT_SOURCE_NAME, e);
            throw new IllegalStateException("I/O error during sourceId lookup", e);
        }
    }

    public boolean addEventToSource(final Long sourceId, final Event event, final HttpServletRequest request) {
        final String url = getBaseUrl(request) + "/api/v2/eventconf/sources/" + sourceId + "/events";
        final HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("Cookie", getCookie(request));
        httpPost.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.getMimeType());

        final String eventXml = marshalToXmlString(event);
        httpPost.setEntity(new StringEntity(eventXml, ContentType.APPLICATION_XML));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            final int statusCode = response.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            switch (statusCode) {
                case HttpStatus.SC_CREATED:
                    LOGGER.info("✅ Event created successfully for sourceId={}", sourceId);
                    return true;
                case HttpStatus.SC_NOT_FOUND:
                    LOGGER.warn("⚠️ EventConf source not found for sourceId={}", sourceId);
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                    LOGGER.error("❌ Invalid event data: {}", responseBody);
                    break;
                default:
                    LOGGER.error("⚠️ Unexpected response: HTTP {}, Body: {}", statusCode, responseBody);
            }
            return false;

        } catch (IOException e) {
            LOGGER.error("I/O error while adding event for sourceId={}", sourceId, e);
            throw new IllegalStateException("I/O error during event creation", e);
        }
    }


    private String getBaseUrl(final HttpServletRequest request) {
        return String.format("%s://%s:%d%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                request.getContextPath());
    }

    private String getCookie(final HttpServletRequest request) {
        if (request.getCookies() == null) {
            LOGGER.error("No cookies found in request.");
            throw new IllegalStateException("No session cookie found");
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equalsIgnoreCase(c.getName()))
                .findFirst()
                .map(c -> COOKIE_NAME + "=" + c.getValue())
                .orElseThrow(() -> {
                    LOGGER.error("Session cookie '{}' not found", COOKIE_NAME);
                    return new IllegalStateException("Session cookie not found");
                });
    }

    private byte[] marshalToXmlBytes(final Object object) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {

            JaxbUtils.marshal(object, writer);
            return outputStream.toByteArray();

        } catch (IOException e) {
            LOGGER.error("Error marshalling object to XML bytes", e);
            throw new IllegalStateException("Failed to serialize object to XML", e);
        }
    }

    private String marshalToXmlString(final Object object) {
        try {
            final JAXBContext context = JAXBContext.newInstance(object.getClass());
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            final StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();

        } catch (JAXBException e) {
            LOGGER.error("Error marshalling object to XML string", e);
            throw new IllegalStateException("Failed to marshal object", e);
        }
    }

    private boolean executeHttpRequest(final HttpUriRequest request, final String actionDescription) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            final int statusCode = response.getStatusLine().getStatusCode();
            final String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                LOGGER.info("✅ Successfully completed {}. HTTP {}", actionDescription, statusCode);
                LOGGER.debug("Response: {}", responseBody);
                return true;
            }

            LOGGER.error("❌ {} failed. HTTP {}, Body: {}", actionDescription, statusCode, responseBody);
            return false;

        } catch (IOException e) {
            LOGGER.error("I/O error during {}", actionDescription, e);
            throw new IllegalStateException("I/O error during " + actionDescription, e);
        }
    }
}

package org.opennms.web.rest.v2;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.ParsedEventEntry;
import org.opennms.web.rest.v2.api.EventConfUploadRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Component
public class EventConfUploadRestService implements EventConfUploadRestApi {

    @Autowired
    private EventConfDao eventConfDao;
    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;


    @Override
    @Transactional
    public Response uploadEventConfFiles(final List<Attachment> attachments,final String comments, final SecurityContext securityContext) {
        final String username = getUsername(securityContext);
        final Date now = new Date();
        int fileOrder = 1;

        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> errorList = new ArrayList<>();

        Map<String, List<ParsedEventEntry>> parsedFiles;
        try {
            parsedFiles = parseUploadedEventFiles(attachments);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Failed to parse uploaded files",
                            "details", e.getMessage()
                    ))
                    .build();
        }

        for (final Map.Entry<String, List<ParsedEventEntry>> entry : parsedFiles.entrySet()) {
            final var  filename = entry.getKey();
            final var  events = entry.getValue();
            final var  vendor = events.get(0).getVendor();

            try {
                eventConfPersistenceService.persistEventConfFile(events, new EventConfSourceMetadataDto.Builder()
                                .filename(filename)
                                .eventCount(events.size())
                                .fileOrder(fileOrder++)
                                .username(username)
                                .now(now)
                                .vendor(vendor)
                                .description(comments != null ? comments : "")
                        .build());
                successList.add(buildSuccessResponse(filename, events));
            } catch (Exception ex) {
                errorList.add(buildErrorResponse(filename, ex));
            }
        }


        return Response.ok(Map.of(
                "success", successList,
                "errors", errorList
        )).build();
    }



    private Map<String, List<ParsedEventEntry>> parseUploadedEventFiles(final List<Attachment> attachments) {
        Map<String, Attachment> fileMap = attachments.stream()
                .collect(Collectors.toMap(
                        a -> a.getContentDisposition().getParameter("filename"),
                        a -> a,
                        (a1, a2) -> a1, // if duplicate, keep the first
                        LinkedHashMap::new
                ));

        final var  eventconfXml = fileMap.remove("eventconf.xml");
        final var orderedFiles = determineFileOrder(eventconfXml, fileMap.keySet());

        Map<String, List<ParsedEventEntry>> result = new LinkedHashMap<>();
        for (final var  fileName : orderedFiles) {
            Attachment attachment = fileMap.get(fileName);
            if (attachment == null) continue;

            try (InputStream stream = attachment.getObject(InputStream.class)) {
                result.put(fileName, parseEventFile(stream));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse file: " + fileName, e);
            }
        }
        return result;
    }

    private List<String> determineFileOrder(final Attachment eventconfXmlAttachment, final Set<String> uploadedFiles) {
        List<String> ordered = new ArrayList<>();

        if (eventconfXmlAttachment != null) {
            try (InputStream stream = eventconfXmlAttachment.getObject(InputStream.class)) {
                List<String> fromXmlRaw = parseOrderingFromEventconfXml(stream);

                // Normalize XML entries to match uploaded file names
                List<String> fromXml = fromXmlRaw.stream()
                        .map(path -> path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path)
                        .toList();

                ordered.addAll(fromXml);

                uploadedFiles.stream()
                        .filter(f -> !fromXml.contains(f))
                        .forEach(ordered::add);

            } catch (Exception e) {
                throw new RuntimeException("Invalid eventconf.xml format", e);
            }
        } else {
            ordered.addAll(uploadedFiles);
        }

        return ordered;
    }



    private List<ParsedEventEntry> parseEventFile(final InputStream inputStream) throws Exception {
        Document doc = newDocument(inputStream);
        NodeList eventNodes = doc.getElementsByTagNameNS("http://xmlns.opennms.org/xsd/eventconf", "event");

        List<ParsedEventEntry> entries = new ArrayList<>();
        for (int i = 0; i < eventNodes.getLength(); i++) {
            Element el = (Element) eventNodes.item(i);
            final var uei = getTagText(el, "uei");
            entries.add(ParsedEventEntry.builder()
                    .uei(uei)
                    .eventLabel(getTagText(el, "event-label"))
                    .description(getTagText(el, "descr"))
                    .enabled(true)
                    .vendor(extractVendorFromUei(uei))
                    .xmlContent(nodeToString(el))
                    .build());
        }
        return entries;
    }

    private Document newDocument(final InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(inputStream);
    }

    private List<String> parseOrderingFromEventconfXml(final InputStream xmlStream) throws Exception {
        Document doc = newDocument(xmlStream);
        NodeList nodes = doc.getElementsByTagNameNS("http://xmlns.opennms.org/xsd/eventconf", "event-file");

        List<String> ordered = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            String name = nodes.item(i).getTextContent().trim();
            if (!name.isEmpty()) ordered.add(name);
        }
        return ordered;
    }

    private String getTagText(final Element parent, final String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : null;
    }

    private String nodeToString(final Node node) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("Error converting node to string", e);
        }
    }

    private String getUsername(final SecurityContext context) {
        return (context != null && context.getUserPrincipal() != null)
                ? context.getUserPrincipal().getName()
                : "unknown";
    }
    private Map<String, Object> buildSuccessResponse(String filename, List<ParsedEventEntry> events) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        entry.put("eventCount", events.size());

        List<Map<String, ? extends Serializable>> eventSummaries = events.stream().map(e -> Map.of(
                "uei", e.getUei(),
                "label", e.getEventLabel(),
                "description", e.getDescription(),
                "enabled", e.getEnabled()
        )).collect(Collectors.toList());

        entry.put("events", eventSummaries);
        return entry;
    }

    private Map<String, Object> buildErrorResponse(String filename, Exception ex) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        entry.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return entry;
    }

    private String extractVendorFromUei(final String uei) {
        if (uei == null || uei.isBlank()) {
            return "unknown";
        }

        // Example format: uei.opennms.org/internal/topology/linkDown
        if (uei.startsWith("uei.")) {
            String[] segments = uei.split("\\.");
            if (segments.length > 1 && !segments[1].isBlank()) {
                return segments[1];
            }
        }

        return "unknown";
    }


}

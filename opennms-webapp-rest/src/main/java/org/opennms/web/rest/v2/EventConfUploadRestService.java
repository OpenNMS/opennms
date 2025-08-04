package org.opennms.web.rest.v2;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.web.rest.v2.api.EventConfUploadRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EventConfUploadRestService implements EventConfUploadRestApi {

    @Autowired
    private DefaultEventConfDao eventConfDao;

    @Override
    public Response uploadEventConfFiles(final List<Attachment> attachments, final SecurityContext securityContext) {
        Map<String, List<ParsedEventEntry>> parsedFiles = parseUploadedEventFiles(attachments);

        StringBuilder sb = new StringBuilder("Parsed Event Entries:\n\n");
        parsedFiles.forEach((filename, entries) -> {
            sb.append("File: ").append(filename).append("\n");
            entries.forEach(entry -> {
                sb.append("  UEI         : ").append(entry.getUei()).append("\n");
                sb.append("  Label       : ").append(entry.getEventLabel()).append("\n");
                sb.append("  Description : ").append(entry.getDescription()).append("\n");
                sb.append("  Enabled     : ").append(entry.getEnabled()).append("\n");
                sb.append("--------------------------------------------------\n");
            });
            sb.append("\n");
        });

        return null;
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
                        .collect(Collectors.toList());

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
            entries.add(ParsedEventEntry.builder()
                    .uei(getTagText(el, "uei"))
                    .eventLabel(getTagText(el, "event-label"))
                    .description(getTagText(el, "descr"))
                    .enabled(true)
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

    public static class ParsedEventEntry {
        private final String uei;
        private final String eventLabel;
        private final String description;
        private final Boolean enabled;
        private final String xmlContent;

        private ParsedEventEntry(Builder builder) {
            this.uei = builder.uei;
            this.eventLabel = builder.eventLabel;
            this.description = builder.description;
            this.enabled = builder.enabled;
            this.xmlContent = builder.xmlContent;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String uei;
            private String eventLabel;
            private String description;
            private Boolean enabled = true;
            private String xmlContent;

            public Builder uei(String uei) {
                this.uei = uei;
                return this;
            }

            public Builder eventLabel(String eventLabel) {
                this.eventLabel = eventLabel;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder enabled(Boolean enabled) {
                this.enabled = enabled;
                return this;
            }

            public Builder xmlContent(String xmlContent) {
                this.xmlContent = xmlContent;
                return this;
            }

            public ParsedEventEntry build() {
                return new ParsedEventEntry(this);
            }
        }

        // Getters if needed (can generate using Lombok as well)
        public String getUei() { return uei; }
        public String getEventLabel() { return eventLabel; }
        public String getDescription() { return description; }
        public Boolean getEnabled() { return enabled; }
        public String getXmlContent() { return xmlContent; }
    }
}

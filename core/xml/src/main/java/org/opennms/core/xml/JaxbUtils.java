/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class JaxbUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbUtils.class);

    public enum MarshallerProfile {
        /**
         * Compact output without marshal-time XSD validation. Use for RPC, sink, tracing,
         * and other in-process wire payloads where objects are already constructed in Java.
         */
        WIRE,
        /**
         * Pretty-printed output with marshal-time XSD validation when an XSD is available.
         * Use for config files and other human-oriented XML persisted to disk.
         */
        CONFIG
    }

    private static final Class<?>[] EMPTY_CLASS_LIST = new Class<?>[0];
    private static final Source[] EMPTY_SOURCE_LIST = new Source[0];

    protected static final class LoggingValidationEventHandler implements ValidationEventHandler {

        protected LoggingValidationEventHandler() {
        }

        @Override
        public boolean handleEvent(final ValidationEvent event) {
            LOG.trace("event = {}", event, event.getLinkedException());
            return false;
        }
    }

    private static final MarshallingExceptionTranslator EXCEPTION_TRANSLATOR = new MarshallingExceptionTranslator();
    private static ThreadLocal<Map<Class<?>, Map<MarshallerProfile, Marshaller>>> m_marshallers = new ThreadLocal<>();
    private static ThreadLocal<Map<Class<?>, Unmarshaller>> m_unMarshallers = new ThreadLocal<Map<Class<?>, Unmarshaller>>();
    private static final ThreadLocal<DocumentBuilder> m_threadLocalDocumentBuilder = new ThreadLocal<>();
    private static final Map<Class<?>,JAXBContext> m_contexts = Collections.synchronizedMap(new WeakHashMap<Class<?>,JAXBContext>());
    private static final Map<Class<?>,Schema> m_schemas = Collections.synchronizedMap(new WeakHashMap<Class<?>,Schema>());
    private static final Map<String,Class<?>> m_elementClasses = Collections.synchronizedMap(new WeakHashMap<String,Class<?>>());
    private static final boolean VALIDATE_IF_POSSIBLE = true;

    private JaxbUtils() {
    }

    public static String marshal(final Object obj) {
        final StringWriter jaxbWriter = new StringWriter();
        marshal(obj, jaxbWriter);
        return jaxbWriter.toString();
    }

    public static void marshal(final Object obj, final File file) throws IOException {
        /*
         * Marshal to a string first, then write the string to the file.
         * This way the original configuration isn't lost if the XML from the
         * marshal is hosed.
         */
        String xmlString = marshal(obj);

        if (xmlString != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            fileWriter.write(xmlString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    public static Class<?> getClassForElement(final String elementName) {
        if (elementName == null) return null;

        final Class<?> existing = m_elementClasses.get(elementName);
        if (existing != null) return existing;

        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(XmlRootElement.class));
        for (final BeanDefinition bd : scanner.findCandidateComponents("org.opennms")) {
            final String className = bd.getBeanClassName();
            try {
                final Class<?> clazz = Class.forName(className);
                final XmlRootElement annotation = clazz.getAnnotation(XmlRootElement.class);
                if (annotation == null) {
                    LOG.warn("Somehow found class {} but it has no @XmlRootElement annotation! Skipping.", className);
                    continue;
                }
                if (elementName.equalsIgnoreCase(annotation.name())) {
                    LOG.trace("Found class {} for element name {}", className, elementName);
                    m_elementClasses.put(elementName, clazz);
                    return clazz;
                }
            } catch (final ClassNotFoundException e) {
                LOG.warn("Unable to get class object from class name {}. Skipping.", className, e);
            }
        }
        return null;
    }

    public static <T> List<String> getNamespacesForClass(final Class<T> clazz) {
        final List<String> namespaces = new ArrayList<>();
        final XmlSeeAlso seeAlso = clazz.getAnnotation(XmlSeeAlso.class);
        if (seeAlso != null) {
            for (final Class<?> c : seeAlso.value()) {
                namespaces.addAll(getNamespacesForClass(c));
            }
        }
        return namespaces;
    }

    public static void marshal(final Object obj, final Writer writer) {
        final Marshaller jaxbMarshaller = getMarshallerFor(obj, null);
        try {
            jaxbMarshaller.marshal(obj, writer);
        } catch (final JAXBException e) {
            throw EXCEPTION_TRANSLATOR.translate("marshalling " + obj.getClass().getSimpleName(), e);
        } catch (final FactoryConfigurationError e) {
            throw EXCEPTION_TRANSLATOR.translate("marshalling " + obj.getClass().getSimpleName(), e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final File file) {
        return unmarshal(clazz, file, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final File file, final boolean validate) {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            return unmarshal(clazz, new InputSource(reader), null, validate, false);
        } catch (final FileNotFoundException e) {
            throw EXCEPTION_TRANSLATOR.translate("reading " + file, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final Reader reader) {
        return unmarshal(clazz, reader, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final Reader reader, final boolean validate) {
        return unmarshal(clazz, new InputSource(reader), null, validate, false);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputStream stream) {
        try (final Reader reader = new InputStreamReader(stream)) {
            return unmarshal(clazz, reader, VALIDATE_IF_POSSIBLE);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("reading stream", e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputStream stream, final boolean validate) {
        try (final Reader reader = new InputStreamReader(stream)) {
            return unmarshal(clazz, new InputSource(reader), null, validate, false);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("reading stream", e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final String xml) {
        return unmarshal(clazz, xml, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final String xml, final boolean validate) {
        final StringReader sr = new StringReader(xml);
        final InputSource is = new InputSource(sr);
        try {
            return unmarshal(clazz, is, null, validate, false);
        } finally {
            IOUtils.closeQuietly(sr);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final Resource resource) {
        return unmarshal(clazz, resource, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final Resource resource, final boolean validate) {
        try {
            return unmarshal(clazz, new InputSource(resource.getInputStream()), null, validate, false);
        } catch (final IOException e) {
            throw EXCEPTION_TRANSLATOR.translate("getting a configuration resource from spring", e);
        }
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource) {
        return unmarshal(clazz, inputSource, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final boolean validate) {
        return unmarshal(clazz, inputSource, null, validate, false);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final JAXBContext jaxbContext) {
        return unmarshal(clazz, inputSource, jaxbContext, VALIDATE_IF_POSSIBLE, false);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final JAXBContext jaxbContext, boolean disableDOCTYPE) {
        return unmarshal(clazz, inputSource, jaxbContext, VALIDATE_IF_POSSIBLE, disableDOCTYPE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final JAXBContext jaxbContext, final boolean validate, final boolean disableDOCTYPE) {
        final Unmarshaller um = getUnmarshallerFor(clazz, jaxbContext, validate);

        LOG.trace("unmarshalling class {} from input source {} with unmarshaller {}", clazz.getSimpleName(), inputSource, um);
        try {
            final XMLFilter filter = getXMLFilterForClass(clazz, disableDOCTYPE);
            final SAXSource source = new SAXSource(filter, inputSource);

            um.setEventHandler(new LoggingValidationEventHandler());

            final JAXBElement<T> element = um.unmarshal(source, clazz);
            return element.getValue();
        } catch (final SAXException e) {
            throw EXCEPTION_TRANSLATOR.translate("creating an XML reader object", e);
        } catch (final JAXBException e) {
            throw EXCEPTION_TRANSLATOR.translate("unmarshalling an object (" + clazz.getSimpleName() + ")", e);
        }
    }

    public static <T> String getNamespaceForClass(final Class<T> clazz) {
        final XmlSchema schema = clazz.getPackage().getAnnotation(XmlSchema.class);
        if (schema != null) {
            final String namespace = schema.namespace();
            if (namespace != null && !"".equals(namespace)) {
                return namespace;
            }
        }
        return null;
    }

    public static <T> XMLFilter getXMLFilterForClass(final Class<T> clazz, boolean disableDOCTYPE) throws SAXException {
        final String namespace = getNamespaceForClass(clazz);
        XMLFilter filter = namespace == null? new SimpleNamespaceFilter("", false) : new SimpleNamespaceFilter(namespace, true);

        LOG.trace("namespace filter for class {}: {}", clazz, filter);
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        if (disableDOCTYPE) {
            xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        }
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        filter.setParent(xmlReader);
        return filter;
    }

    public static XMLFilter getXMLFilterForNamespace(final String namespace) throws SAXException {
        XMLFilter filter = namespace == null ? new SimpleNamespaceFilter("", false) : new SimpleNamespaceFilter(namespace, true);

        LOG.trace("namespace filter for namespace {}: {}", namespace, filter);
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        filter.setParent(xmlReader);
        return filter;
    }

    /**
     * Marshaller for machine-oriented XML (RPC/sink payloads): no pretty-print, no schema validation.
     */
    public static Marshaller createWireMarshaller(final JAXBContext context) throws JAXBException {
        return createMarshaller(context, null, MarshallerProfile.WIRE);
    }

    /**
     * Marshal a JAXB root object to a DOM element using {@link MarshallerProfile#WIRE}.
     *
     * <p>Package-private so callers outside {@code org.opennms.core.xml} must pass an explicit
     * {@link MarshallerProfile}. Config-file embeds should use
     * {@link #marshalToDomElement(Object, MarshallerProfile)} with {@link MarshallerProfile#CONFIG}
     * or {@link JaxbClassObjectAdapter}.</p>
     */
    static Element marshalToDomElement(final Object obj) {
        return marshalToDomElement(obj, MarshallerProfile.WIRE);
    }

    /**
     * Marshal a JAXB root object to a DOM element for embedding without marshal-to-string +
     * {@link DocumentBuilder#parse}. The profile selects compact wire output or validated config output.
     */
    public static Element marshalToDomElement(final Object obj, final MarshallerProfile profile) {
        try {
            final Class<?> clazz = obj.getClass();
            final Marshaller marshaller = getMarshallerFor(clazz, null, profile);
            final Document doc = getThreadLocalDocumentBuilder().newDocument();
            final DOMResult domResult = new DOMResult(doc);
            marshaller.marshal(obj, domResult);
            final Element root = doc.getDocumentElement();
            if (root == null) {
                throw new IllegalStateException("JAXB produced no document element for " + clazz.getName());
            }
            return root;
        } catch (final JAXBException e) {
            throw EXCEPTION_TRANSLATOR.translate("marshalling embedded " + obj.getClass().getSimpleName(), e);
        }
    }

    private static DocumentBuilder getThreadLocalDocumentBuilder() {
        DocumentBuilder builder = m_threadLocalDocumentBuilder.get();
        if (builder != null) {
            return builder;
        }
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            builder = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw EXCEPTION_TRANSLATOR.translate("creating DocumentBuilder for embedded JAXB marshal", e);
        }
        m_threadLocalDocumentBuilder.set(builder);
        return builder;
    }

    public static Marshaller getMarshallerFor(final Object obj, final JAXBContext jaxbContext) {
        return getMarshallerFor(obj, jaxbContext, MarshallerProfile.CONFIG);
    }

    public static Marshaller getMarshallerFor(final Object obj, final JAXBContext jaxbContext, final MarshallerProfile profile) {
        final Class<?> clazz = (Class<?>)(obj instanceof Class<?> ? obj : obj.getClass());

        if (jaxbContext == null) {
            Map<Class<?>, Map<MarshallerProfile, Marshaller>> marshallers = m_marshallers.get();
            if (marshallers == null) {
                marshallers = new WeakHashMap<>();
                m_marshallers.set(marshallers);
            }
            final Map<MarshallerProfile, Marshaller> byProfile = marshallers.get(clazz);
            if (byProfile != null && byProfile.containsKey(profile)) {
                LOG.trace("found marshaller for {} ({})", clazz, profile);
                return byProfile.get(profile);
            }
            LOG.trace("creating marshaller for {} ({})", clazz, profile);
            try {
                final Marshaller marshaller = createMarshaller(getContextFor(clazz), clazz, profile);
                Map<MarshallerProfile, Marshaller> profileMap = byProfile;
                if (profileMap == null) {
                    profileMap = new EnumMap<>(MarshallerProfile.class);
                    marshallers.put(clazz, profileMap);
                }
                profileMap.put(profile, marshaller);
                return marshaller;
            } catch (final JAXBException e) {
                throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
            }
        }

        LOG.trace("creating marshaller for {} ({}) with explicit context", clazz, profile);
        try {
            return createMarshaller(jaxbContext, clazz, profile);
        } catch (final JAXBException e) {
            throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
        }
    }

    private static Marshaller createMarshaller(final JAXBContext context, final Class<?> clazz, final MarshallerProfile profile) throws JAXBException {
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, profile == MarshallerProfile.CONFIG);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        if (context.getClass().getName().startsWith("org.eclipse.persistence.jaxb")) {
            marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, new EmptyNamespacePrefixMapper());
            marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, Boolean.TRUE);
        }
        if (profile == MarshallerProfile.WIRE) {
            marshaller.setSchema(null);
        } else {
            marshaller.setSchema(getValidatorFor(clazz));
        }
        return marshaller;
    }

    /**
     * Get a JAXB unmarshaller for the given object.  If no JAXBContext is provided,
     * JAXBUtils will create and cache a context for the given object.
     * @param obj The object type to be unmarshalled.
     * @param jaxbContext An optional JAXB context to create the unmarshaller from.
     * @param validate TODO
     * @return an Unmarshaller
     */
    public static Unmarshaller getUnmarshallerFor(final Object obj, final JAXBContext jaxbContext, boolean validate) {
        final Class<?> clazz = (Class<?>)(obj instanceof Class<?> ? obj : obj.getClass());

        Unmarshaller unmarshaller = null;

        Map<Class<?>, Unmarshaller> unmarshallers = m_unMarshallers.get();
        if (jaxbContext == null) {
            if (unmarshallers == null) {
                unmarshallers = new WeakHashMap<Class<?>, Unmarshaller>();
                m_unMarshallers.set(unmarshallers);
            }
            if (unmarshallers.containsKey(clazz)) {
                LOG.trace("found unmarshaller for {}", clazz);
                unmarshaller = unmarshallers.get(clazz);
            }
        }

        if (unmarshaller == null) {
            try {
                if (jaxbContext == null) {
                    unmarshaller = getContextFor(clazz).createUnmarshaller();
                } else {
                    unmarshaller = jaxbContext.createUnmarshaller();
                }
            } catch (final JAXBException e) {
                throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
            }
        }

        LOG.trace("created unmarshaller for {}", clazz);

        if (validate) {
            final Schema schema = getValidatorFor(clazz);
            if (schema == null) {
                LOG.trace("Validation is enabled, but no XSD found for class {}", clazz.getSimpleName());
            }
            unmarshaller.setSchema(schema);
        }
        if (jaxbContext == null) unmarshallers.put(clazz, unmarshaller);

        return unmarshaller;
    }

    private static Collection<Class<?>> getAllRelatedClasses(final Class<?> clazz) {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(clazz);

        // Get any XmlSeeAlsos on the class
        final XmlSeeAlso seeAlso = clazz.getAnnotation(XmlSeeAlso.class);
        if (seeAlso != null && seeAlso.value() != null) {
            for (final Class<?> c : seeAlso.value()) {
                classes.addAll(getAllRelatedClasses(c));
            }
        }

        LOG.trace("getAllRelatedClasses({}): {}", clazz, classes);
        return classes;
    }

    public static JAXBContext getContextFor(final Class<?> clazz) throws JAXBException {
        LOG.trace("Getting context for class {}", clazz);
        final JAXBContext context;
        if (m_contexts.containsKey(clazz)) {
            context = m_contexts.get(clazz);
        } else {
            final Collection<Class<?>> allRelatedClasses = getAllRelatedClasses(clazz);
            LOG.trace("Creating new context for classes: {}", allRelatedClasses);
            context = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(allRelatedClasses.toArray(EMPTY_CLASS_LIST), null);
            LOG.trace("Context for {}: {}", allRelatedClasses, context);
            m_contexts.put(clazz, context);
        }
        return context;
    }

    private static List<String> getSchemaFilesFor(final Class<?> clazz) {
        final List<String> schemaFiles = new ArrayList<>();
        for (final Class<?> c : getAllRelatedClasses(clazz)) {
            final ValidateUsing annotation = c.getAnnotation(ValidateUsing.class);
            if (annotation == null || annotation.value() == null) {
                LOG.debug("@ValidateUsing is missing from class {}", c);
                continue;
            } else {
                schemaFiles.add(annotation.value());
            }
        }
        return schemaFiles;
    }

    private static Schema getValidatorFor(final Class<?> clazz) {
        LOG.trace("finding XSD for class {}", clazz);

        if (m_schemas.containsKey(clazz)) {
            return m_schemas.get(clazz);
        }

        final List<Source> sources = new ArrayList<>();
        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        for (final String schemaFileName : getSchemaFilesFor(clazz)) {
            InputStream schemaInputStream = null;
            try {
                if (schemaInputStream == null) {
                    final File schemaFile = new File(System.getProperty("opennms.home") + "/share/xsds/" + schemaFileName);
                    if (schemaFile.exists()) {
                        LOG.trace("Found schema file {} related to {}", schemaFile, clazz);
                        schemaInputStream = new FileInputStream(schemaFile);
                    };
                }
                if (schemaInputStream == null) {
                    final File schemaFile = new File("target/xsds/" + schemaFileName);
                    if (schemaFile.exists()) {
                        LOG.trace("Found schema file {} related to {}", schemaFile, clazz);
                        schemaInputStream = new FileInputStream(schemaFile);
                    };
                }
                if (schemaInputStream == null) {
                    URL schemaResource = Thread.currentThread().getContextClassLoader().getResource("xsds/" + schemaFileName);
                    if (schemaResource == null) {
                        schemaResource = clazz.getClassLoader().getResource("xsds/" + schemaFileName);
                    }
                    if (schemaResource == null) {
                        LOG.debug("Unable to load resource xsds/{} from the classpath.", schemaFileName);
                    } else {
                        LOG.trace("Found schema resource {} related to {}", schemaResource, clazz);
                        schemaInputStream = schemaResource.openStream();
                    }
                }
                if (schemaInputStream == null) {
                    LOG.trace("Did not find a suitable XSD.  Skipping.");
                    continue;
                } else {
                    sources.add(new StreamSource(schemaInputStream));
                }
            } catch (final Throwable t) {
                LOG.warn("an error occurred while attempting to load {} for validation", schemaFileName);
                continue;
            }
        }

        if (sources.size() == 0) {
            LOG.debug("No schema files found for validating {}", clazz);
            return null;
        }

        LOG.trace("Schema sources: {}", sources);

        try {
            final Schema schema = factory.newSchema(sources.toArray(EMPTY_SOURCE_LIST));
            m_schemas.put(clazz, schema);
            return schema;
        } catch (final SAXException e) {
            LOG.warn("an error occurred while attempting to load schema validation files for class {}", clazz, e);
            return null;
        }
    }

    public static <T> T duplicateObject(T obj, final Class<T> clazz) {
        return unmarshal(clazz, marshal(obj));
    }
}

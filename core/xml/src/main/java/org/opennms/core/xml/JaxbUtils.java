/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class JaxbUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(JaxbUtils.class);
	
    private static final class LoggingValidationEventHandler implements ValidationEventHandler {

		private LoggingValidationEventHandler() {
		}

		@Override
		public boolean handleEvent(final ValidationEvent event) {
			LOG.trace("event = {}", event, event.getLinkedException());
			return false;
		}
	}

	private static final MarshallingExceptionTranslator EXCEPTION_TRANSLATOR = new MarshallingExceptionTranslator();
	private static ThreadLocal<Map<Class<?>, Marshaller>> m_marshallers = new ThreadLocal<Map<Class<?>, Marshaller>>();
	private static ThreadLocal<Map<Class<?>, Unmarshaller>> m_unMarshallers = new ThreadLocal<Map<Class<?>, Unmarshaller>>();
	private static final Map<Class<?>,JAXBContext> m_contexts = Collections.synchronizedMap(new WeakHashMap<Class<?>,JAXBContext>());
	private static final Map<Class<?>,Schema> m_schemas = Collections.synchronizedMap(new WeakHashMap<Class<?>,Schema>());
    private static final boolean VALIDATE_IF_POSSIBLE = true;

	private JaxbUtils() {
	}

	public static String marshal(final Object obj) {
		final StringWriter jaxbWriter = new StringWriter();
        marshal(obj, jaxbWriter);
		return jaxbWriter.toString();
	}

	public static void marshal(final Object obj, final Writer writer) {
		final Marshaller jaxbMarshaller = getMarshallerFor(obj, null);
		try {
			jaxbMarshaller.marshal(obj, writer);
		} catch (final JAXBException e) {
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
			return unmarshal(clazz, new InputSource(reader), null, validate);
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
		return unmarshal(clazz, new InputSource(reader), null, validate);
	}

	public static <T> T unmarshal(final Class<T> clazz, final String xml) {
        return unmarshal(clazz, xml, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final String xml, final boolean validate) {
		final StringReader sr = new StringReader(xml);
		final InputSource is = new InputSource(sr);
		try {
			return unmarshal(clazz, is, null, validate);
		} finally {
			IOUtils.closeQuietly(sr);
		}
	}

	public static <T> T unmarshal(final Class<T> clazz, final Resource resource) {
        return unmarshal(clazz, resource, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final Resource resource, final boolean validate) {
		try {
			return unmarshal(clazz, new InputSource(resource.getInputStream()), null, validate);
		} catch (final IOException e) {
			throw EXCEPTION_TRANSLATOR.translate("getting a configuration resource from spring", e);
		}
	}

	public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource) {
        return unmarshal(clazz, inputSource, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final boolean validate) {
		return unmarshal(clazz, inputSource, null, validate);
	}

	public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final JAXBContext jaxbContext) {
        return unmarshal(clazz, inputSource, jaxbContext, VALIDATE_IF_POSSIBLE);
    }

    public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource, final JAXBContext jaxbContext, final boolean validate) {
		final Unmarshaller um = getUnmarshallerFor(clazz, jaxbContext, validate);
		
		LOG.trace("unmarshalling class {} from input source {} with unmarshaller {}", clazz.getSimpleName(), inputSource, um);
		try {
			final XMLFilter filter = getXMLFilterForClass(clazz);
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

	public static <T> XMLFilter getXMLFilterForClass(final Class<T> clazz) throws SAXException {
		final XMLFilter filter;
		final XmlSchema schema = clazz.getPackage().getAnnotation(XmlSchema.class);
		if (schema != null) {
			final String namespace = schema.namespace();
			if (namespace != null && !"".equals(namespace)) {
				LOG.trace("found namespace {} for class {}", namespace, clazz);
				filter = new SimpleNamespaceFilter(namespace, true);
			} else {
				filter = new SimpleNamespaceFilter("", false);
			}
		} else {
			filter = new SimpleNamespaceFilter("", false);
		}

		final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		filter.setParent(xmlReader);
		return filter;
	}

	public static Marshaller getMarshallerFor(final Object obj, final JAXBContext jaxbContext) {
		final Class<?> clazz = (Class<?>)(obj instanceof Class<?> ? obj : obj.getClass());

		Map<Class<?>, Marshaller> marshallers = m_marshallers.get();
		if (jaxbContext == null) {
			if (marshallers == null) {
				marshallers = new WeakHashMap<Class<?>, Marshaller>();
				m_marshallers.set(marshallers);
			}
			if (marshallers.containsKey(clazz)) {
				LOG.trace("found unmarshaller for {}", clazz);
				return marshallers.get(clazz);
			}
		}
		LOG.trace("creating unmarshaller for {}", clazz);

		try {
			final JAXBContext context;
			if (jaxbContext == null) {
				context = getContextFor(clazz);
			} else {
				context = jaxbContext;
			}
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			final Schema schema = getValidatorFor(clazz);
			marshaller.setSchema(schema);
			if (jaxbContext == null) marshallers.put(clazz, marshaller);
			
			return marshaller;
		} catch (JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
		}
	}

	/**
	 * Get a JAXB unmarshaller for the given object.  If no JAXBContext is provided,
	 * JAXBUtils will create and cache a context for the given object.
	 * @param obj The object type to be unmarshaled.
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
                final JAXBContext context;
                if (jaxbContext == null) {
                        context = getContextFor(clazz);
                } else {
                    context = jaxbContext;
                }
                unmarshaller = context.createUnmarshaller();
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

	private static JAXBContext getContextFor(final Class<?> clazz) throws JAXBException {
		final JAXBContext context;
		if (m_contexts.containsKey(clazz)) {
			context = m_contexts.get(clazz);
		} else {
			context = JAXBContext.newInstance(clazz);
			m_contexts.put(clazz, context);
		}
		return context;
	}

	private static Schema getValidatorFor(final Class<?> origClazz) {
		final Class<?> clazz = (Class<?>)(origClazz instanceof Class<?> ? origClazz : origClazz.getClass());
		LOG.trace("finding XSD for class {}", clazz);

		if (m_schemas.containsKey(clazz)) {
			return m_schemas.get(clazz);
		}

		final ValidateUsing schemaFileAnnotation = clazz.getAnnotation(ValidateUsing.class);
		if (schemaFileAnnotation == null || schemaFileAnnotation.value() == null) {
			return null;
		}
		
		final String schemaFileName = schemaFileAnnotation.value();
		InputStream schemaInputStream = null;
		try {
			final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			if (schemaInputStream == null) {
				final File schemaFile = new File(System.getProperty("opennms.home") + "/share/xsds/" + schemaFileName);
				if (schemaFile.exists()) {
					LOG.trace("using file {}", schemaFile);
					schemaInputStream = new FileInputStream(schemaFile);
				};
			}
			if (schemaInputStream == null) {
				final File schemaFile = new File("target/xsds/" + schemaFileName);
				if (schemaFile.exists()) {
					LOG.trace("using file {}", schemaFile);
					schemaInputStream = new FileInputStream(schemaFile);
				};
			}
			if (schemaInputStream == null) {
				final URL schemaResource = Thread.currentThread().getContextClassLoader().getResource("xsds/" + schemaFileName);
				if (schemaResource == null) {
					LOG.debug("Unable to load resource xsds/{} from the classpath.", schemaFileName);
				} else {
					LOG.trace("using resource {} from classpath", schemaResource);
					schemaInputStream = schemaResource.openStream();
				}
			}
			if (schemaInputStream == null) {
				LOG.trace("Did not find a suitable XSD.  Skipping.");
				return null;
			}
			final Schema schema = factory.newSchema(new StreamSource(schemaInputStream));
			m_schemas.put(clazz, schema);
			return schema;
		} catch (final Throwable t) {
			LOG.warn("an error occurred while attempting to load {} for validation", schemaFileName, t);
			return null;
		} finally {
			IOUtils.closeQuietly(schemaInputStream);
		}
	}
}

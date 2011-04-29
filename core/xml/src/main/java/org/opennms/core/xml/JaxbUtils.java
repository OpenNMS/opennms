package org.opennms.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.LogUtils;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class JaxbUtils {
    private static final MarshallingExceptionTranslator EXCEPTION_TRANSLATOR = new MarshallingExceptionTranslator();
	private static ThreadLocal<Map<Class<?>, Marshaller>> m_marshallers = new ThreadLocal<Map<Class<?>, Marshaller>>();
	private static ThreadLocal<Map<Class<?>, Unmarshaller>> m_unMarshallers = new ThreadLocal<Map<Class<?>, Unmarshaller>>();
	
	private JaxbUtils() {
	}

	public static String marshal(final Object obj) {
		final StringWriter jaxbWriter = new StringWriter();
        marshal(obj, jaxbWriter);
		return jaxbWriter.toString();
	}

	public static void marshal(final Object obj, final Writer writer) {
		final Marshaller jaxbMarshaller = getMarshallerFor(obj);
		try {
			jaxbMarshaller.marshal(obj, writer);
		} catch (final JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("marshalling " + obj.getClass().getSimpleName(), e);
		}
	}

	public static <T> T unmarshal(final Class<T> clazz, final File file) {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			return unmarshal(clazz, new InputSource(reader));
		} catch (final FileNotFoundException e) {
			throw EXCEPTION_TRANSLATOR.translate("reading " + file, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public static <T> T unmarshal(final Class<T> clazz, final Reader reader) {
		return unmarshal(clazz, new InputSource(reader));
	}

	public static <T> T unmarshal(final Class<T> clazz, final String xml) {
		final StringReader sr = new StringReader(xml);
		final InputSource is = new InputSource(sr);
		try {
			return unmarshal(clazz, is);
		} finally {
			IOUtils.closeQuietly(sr);
		}
	}

	public static <T> T unmarshal(final Class<T> clazz, final Resource resource) {
		try {
			return unmarshal(clazz, new InputSource(resource.getInputStream()));
		} catch (final IOException e) {
			throw EXCEPTION_TRANSLATOR.translate("getting a configuration resource from spring", e);
		}
	}
	
	public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource) {
		final Unmarshaller um = getUnmarshallerFor(clazz);

		LogUtils.debugf(JaxbUtils.class, "unmarshalling class %s from input source %s with unmarshaller %s", clazz.getSimpleName(), inputSource, um);
		try {
			XMLFilter filter = getXMLFilterForClass(clazz);
			final SAXSource source = new SAXSource(filter, inputSource);

			um.setEventHandler(new ValidationEventHandler() {
				
				@Override
				public boolean handleEvent(final ValidationEvent event) {
					LogUtils.debugf(this, event.getLinkedException(), "event = %s", event);
					return false;
				}
			});
			
			final JAXBElement<T> element = um.unmarshal(source, clazz);
			return element.getValue();
		} catch (final SAXException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating an XML reader object", e);
		} catch (final JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("unmarshalling an object (" + clazz.getSimpleName() + ")", e);
		}
	}

	public static <T> XMLFilter getXMLFilterForClass(final Class<T> clazz) throws SAXException {
		XMLFilter filter = null;
		final XmlSchema schema = clazz.getPackage().getAnnotation(XmlSchema.class);
		if (schema != null) {
			final String namespace = schema.namespace();
			if (namespace != null && !"".equals(namespace)) {
				LogUtils.debugf(JaxbUtils.class, "found namespace %s for class %s", namespace, clazz);
				filter = new SimpleNamespaceFilter(namespace, true);
			}
		}
		if (filter == null) {
			filter = new SimpleNamespaceFilter("", false);
		}

		final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		filter.setParent(xmlReader);
		return filter;
	}

	public static Marshaller getMarshallerFor(final Object obj) {
		final Class<?> clazz = (Class<?>)(obj instanceof Class? obj : obj.getClass());
		
		Map<Class<?>, Marshaller> marshallers = m_marshallers.get();
		if (marshallers == null) {
			marshallers = new HashMap<Class<?>, Marshaller>();
			m_marshallers.set(marshallers);
		}
		if (marshallers.containsKey(clazz)) {
			LogUtils.debugf(JaxbUtils.class, "found unmarshaller for %s", clazz);
			return marshallers.get(clazz);
		}
		LogUtils.debugf(JaxbUtils.class, "creating unmarshaller for %s", clazz);

		try {
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Marshaller marshaller = context.createMarshaller();
			marshallers.put(clazz, marshaller);
			return marshaller;
		} catch (JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
		}
	}

	public static Unmarshaller getUnmarshallerFor(final Object obj) {
		final Class<?> clazz = (Class<?>)(obj instanceof Class? obj : obj.getClass());

		Map<Class<?>, Unmarshaller> unmarshallers = m_unMarshallers.get();
		if (unmarshallers == null) {
			unmarshallers = new HashMap<Class<?>, Unmarshaller>();
			m_unMarshallers.set(unmarshallers);
		}
		if (unmarshallers.containsKey(clazz)) {
			LogUtils.debugf(JaxbUtils.class, "found unmarshaller for %s", clazz);
			return unmarshallers.get(clazz);
		}
		LogUtils.debugf(JaxbUtils.class, "creating unmarshaller for %s", clazz);

		try {
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshallers.put(clazz, unmarshaller);
			return unmarshaller;
		} catch (JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
		}
	}

}

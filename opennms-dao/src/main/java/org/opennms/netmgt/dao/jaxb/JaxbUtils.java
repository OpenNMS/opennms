package org.opennms.netmgt.dao.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.dao.support.MarshallingExceptionTranslator;
import org.opennms.netmgt.xml.SimpleNamespaceFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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

	public static <T> T unmarshal(final Class<T> clazz, final InputSource inputSource) {
		final Unmarshaller um = getUnmarshallerFor(clazz);
		
		try {
			final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			final SimpleNamespaceFilter filter = new SimpleNamespaceFilter("", false);
			filter.setParent(xmlReader);
			
			final SAXSource source = new SAXSource(xmlReader, inputSource);
			final JAXBElement<T> obj = um.unmarshal(source, clazz);
			return obj.getValue();
		} catch (final SAXException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating an XML reader object", e);
		} catch (final JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("unmarshalling an object (" + clazz.getSimpleName() + ")", e);
		}
	}

	private static Marshaller getMarshallerFor(final Object obj) {
		final Class<?> clazz = (Class<?>)(obj instanceof Class? obj : obj.getClass());
		
		Map<Class<?>, Marshaller> marshallers = m_marshallers.get();
		if (marshallers == null) {
			marshallers = new HashMap<Class<?>, Marshaller>();
			m_marshallers.set(marshallers);
		}
		if (marshallers.containsKey(clazz)) {
			return marshallers.get(clazz);
		}

//		final String packageName = clazz.getPackage().getName();
		try {
//			final JAXBContext context = JAXBContext.newInstance(packageName);
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Marshaller marshaller = context.createMarshaller();
			marshallers.put(clazz, marshaller);
			return marshaller;
		} catch (JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
		}
	}

	private static Unmarshaller getUnmarshallerFor(final Object obj) {
		final Class<?> clazz = (Class<?>)(obj instanceof Class? obj : obj.getClass());
		
		Map<Class<?>, Unmarshaller> unmarshallers = m_unMarshallers.get();
		if (unmarshallers == null) {
			unmarshallers = new HashMap<Class<?>, Unmarshaller>();
			m_unMarshallers.set(unmarshallers);
		}
		if (unmarshallers.containsKey(clazz)) {
			return unmarshallers.get(clazz);
		}

//		final String packageName = clazz.getPackage().getName();
		try {
//			final JAXBContext context = JAXBContext.newInstance(packageName);
			final JAXBContext context = JAXBContext.newInstance(clazz);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshallers.put(clazz, unmarshaller);
			return unmarshaller;
		} catch (JAXBException e) {
			throw EXCEPTION_TRANSLATOR.translate("creating XML marshaller", e);
		}
	}

}

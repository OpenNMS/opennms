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

package org.opennms.core.test.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

@RunWith(Parameterized.class)
abstract public class XmlTest<T> {

	private T m_sampleObject;
	private String m_sampleXml;
	private String m_schemaFile;

	public XmlTest(final T sampleObject, final String sampleXml, final String schemaFile) {
		m_sampleObject = sampleObject;
		m_sampleXml = sampleXml;
		m_schemaFile = schemaFile;
	}
	
	protected T getSampleObject() {
		return m_sampleObject;
	}

	protected String getSampleXml() {
		return m_sampleXml;
	}

	@SuppressWarnings("unchecked")
	private Class<T> getSampleClass() {
		return (Class<T>)getSampleObject().getClass();
	}

	@Before
	public void setUp() {
		MockLogAppender.setupLogging(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
	}

	@Test
	public void marshalCastorAndCompareToXml() throws Exception {
		final String xml = marshalToXmlWithCastor();
		assertXmlEquals(m_sampleXml, xml);
	}

	@Test
	public void marshalJaxbAndCompareToXml() throws Exception {
		final String xml = marshalToXmlWithJaxb();
		assertXmlEquals(m_sampleXml, xml);
	}

	@Test
	public void unmarshalXmlAndCompareToCastor() throws Exception {
		final T obj = CastorUtils.unmarshal(getSampleClass(), new ByteArrayInputStream(m_sampleXml.getBytes()));
		assertTrue("objects should match", getSampleObject().equals(obj));
	}

	@Test
	public void unmarshalXmlAndCompareToJaxb() throws Exception {
		final T obj = JaxbUtils.unmarshal(getSampleClass(), new InputSource(new ByteArrayInputStream(m_sampleXml.getBytes())), null);
		assertTrue("objects should match", getSampleObject().equals(obj));
	}
	
	@Test
	public void marshalCastorUnmarshalJaxb() throws Exception {
		final String xml = marshalToXmlWithCastor();
		
		final T config = JaxbUtils.unmarshal(getSampleClass(), xml);
		LogUtils.debugf(this, "Generated Object: %s", config);
	
		assertTrue("objects should match", config.equals(getSampleObject()));
	}

	@Test
	public void marshalJaxbUnmarshalCastor() throws Exception {
		final String xml = marshalToXmlWithJaxb();
	
		final T config = CastorUtils.unmarshal(getSampleClass(), new ByteArrayInputStream(xml.getBytes()));
		LogUtils.debugf(this, "Generated Object: %s", config);
	
		assertTrue("objects should match", config.equals(getSampleObject()));
	}

	@Test
	public void validateCastorObjectAgainstSchema() throws Exception {
		org.exolab.castor.xml.Unmarshaller unmarshaller = CastorUtils.getUnmarshaller(getSampleClass());
		unmarshaller.setValidation(true);
		@SuppressWarnings("unchecked")
		T obj = (T)unmarshaller.unmarshal(new InputSource(new ByteArrayInputStream(m_sampleXml.getBytes())));
		assertNotNull(obj);
	}

	@Test
	public void validateJaxbXmlAgainstSchema() throws Exception {
		LogUtils.debugf(this, "Validating against XSD: %s", m_schemaFile);
		javax.xml.bind.Unmarshaller unmarshaller = JaxbUtils.getUnmarshallerFor(getSampleClass(), null, true);
        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final Schema schema = factory.newSchema(new StreamSource(m_schemaFile));
		unmarshaller.setSchema(schema);
		unmarshaller.setEventHandler(new ValidationEventHandler() {
			@Override
			public boolean handleEvent(final ValidationEvent event) {
				LogUtils.debugf(this, event.getLinkedException(), "Received validation event: %s", event);
				return false;
			}
		});
		try {
			final InputSource inputSource = new InputSource(new ByteArrayInputStream(m_sampleXml.getBytes()));
			final XMLFilter filter = JaxbUtils.getXMLFilterForClass(getSampleClass());
			final SAXSource source = new SAXSource(filter, inputSource);
			@SuppressWarnings("unchecked")
			T obj = (T)unmarshaller.unmarshal(source);
			assertNotNull(obj);
		} finally {
			unmarshaller.setSchema(null);
		}
	}

	protected void validateXmlString(final String xml) throws Exception {
		if (m_schemaFile == null) {
			LogUtils.warnf(this, "skipping validation, schema file not set");
			return;
		}

		final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		final File schemaFile = new File(m_schemaFile);
		LogUtils.debugf(this, "Validating using schema file: %s", schemaFile);
		final Schema schema = schemaFactory.newSchema(schemaFile);
		
		final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setValidating(true);
		saxParserFactory.setNamespaceAware(true);
		saxParserFactory.setSchema(schema);
		
		assertTrue("make sure our SAX implementation can validate", saxParserFactory.isValidating());
		
		final Validator validator = schema.newValidator();
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		final Source source = new StreamSource(inputStream);
		
		validator.validate(source);
	}

	protected String marshalToXmlWithCastor() {
		LogUtils.debugf(this, "Reference Object: %s", getSampleObject());
		
		final StringWriter writer = new StringWriter();
		CastorUtils.marshalWithTranslatedExceptions(getSampleObject(), writer);
		final String xml = writer.toString();
		LogUtils.debugf(this, "Castor XML: %s", xml);
		return xml;
	}
	
	protected String marshalToXmlWithJaxb() {
		LogUtils.debugf(this, "Reference Object: %s", getSampleObject());
	
		final StringWriter writer = new StringWriter();
		JaxbUtils.marshal(getSampleObject(), writer);
		final String xml = writer.toString();
		LogUtils.debugf(this, "JAXB XML: %s", xml);
		return xml;
	}

	public static void assertXmlEquals(final String xmlA, final String xmlB) throws Exception {
        final List<Difference> differences = getDifferences(xmlA, xmlB);
        if (differences.size() > 0) {
        	LogUtils.debugf(XmlTest.class, "XML:\n\n%s\n\n...does not match XML:\n\n%s", xmlA, xmlB);
        }
        assertEquals("number of XMLUnit differences between the example xml and the generated xml should be 0", 0, differences.size());
	}

	protected static List<Difference> getDifferences(final String xmlA, final String xmlB) throws SAXException, IOException {
		final DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(xmlA, xmlB));
		final List<Difference> retDifferences = new ArrayList<Difference>();
        @SuppressWarnings("unchecked")
		final List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            for (final Difference d : allDifferences) {
            	if (d.getDescription().equals("namespace URI")) {
            		LogUtils.infof(XmlTest.class, "Ignoring namspace difference: %s", d);
            	} else {
                	LogUtils.warnf(XmlTest.class, "Found difference: %s", d);
                	retDifferences.add(d);
            	}
            }
        }
        return retDifferences;
    }

}

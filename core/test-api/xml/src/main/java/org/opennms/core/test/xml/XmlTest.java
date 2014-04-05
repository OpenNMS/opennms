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
import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

@RunWith(Parameterized.class)
abstract public class XmlTest<T> {
    private T m_sampleObject;
    private Object m_sampleXml;
    private String m_schemaFile;

    public XmlTest(final T sampleObject, final Object sampleXml, final String schemaFile) {
        m_sampleObject = sampleObject;
        m_sampleXml = sampleXml;
        m_schemaFile = schemaFile;
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setNormalize(true);
    }

    protected T getSampleObject() {
        return m_sampleObject;
    }

    protected String getSampleXml() throws IOException {
        if (m_sampleXml instanceof File) {
            return IOUtils.toString(((File)m_sampleXml).toURI());
        } else if (m_sampleXml instanceof URI) {
            return IOUtils.toString((URI)m_sampleXml);
        } else if (m_sampleXml instanceof URL) {
            return IOUtils.toString((URL)m_sampleXml);
        } else if (m_sampleXml instanceof InputStream) {
            return IOUtils.toString((InputStream)m_sampleXml);
        } else {
            return m_sampleXml.toString();
        }
    }

    protected ByteArrayInputStream getSampleXmlInputStream() throws IOException {
        return new ByteArrayInputStream(getSampleXml().getBytes());
    }

    protected String getSchemaFile() {
        return m_schemaFile;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getSampleClass() {
        return (Class<T>) getSampleObject().getClass();
    }

    protected boolean ignoreNamespace(final String namespace) {
        return true;
    }

    protected boolean ignorePrefix(final String prefix) {
        return true;
    }

    protected boolean ignoreDifference(final Difference d) {
        if ("text value".equals(d.getDescription())) {
            final String controlValue = d.getControlNodeDetail().getValue();
            final String testValue = d.getTestNodeDetail().getValue();

            return (controlValue == null || controlValue.trim().isEmpty()) &&
                    (testValue    == null || testValue.trim().isEmpty());
        }
        return false;
    }

    protected void validateXmlString(final String xml) throws Exception {
        if (getSchemaFile() == null) {
            LogUtils.warnf(this, "skipping validation, schema file not set");
            return;
        }

        final SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final File schemaFile = new File(getSchemaFile());
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

    @Test
    public void marshalCastorAndCompareToXml() throws Exception {
        final String xml = marshalToXmlWithCastor();
        _assertXmlEquals(getSampleXml(), xml);
    }

    @Test
    public void marshalJaxbAndCompareToXml() throws Exception {
        final String xml = marshalToXmlWithJaxb();
        _assertXmlEquals(getSampleXml(), xml);
    }

    @Test
    public void unmarshalXmlAndCompareToCastor() throws Exception {
        final T obj = CastorUtils.unmarshal(getSampleClass(), getSampleXmlInputStream());
        LogUtils.debugf(this, "Sample object: %s\n\nCastor object: %s", getSampleObject(), obj);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void unmarshalJaxbMarshalJaxb() throws Exception {
        final T obj = JaxbUtils.unmarshal(getSampleClass(), new InputSource(getSampleXmlInputStream()), null);
        final String remarshaled = JaxbUtils.marshal(obj);
        _assertXmlEquals(getSampleXml(), remarshaled);
    }

    @Test
    public void marshalJaxbUnmarshalJaxb() throws Exception {
        final String xml = marshalToXmlWithJaxb();
        final T obj = JaxbUtils.unmarshal(getSampleClass(), xml);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void unmarshalCastorMarshalCastor() throws Exception {
        final T obj = CastorUtils.unmarshal(getSampleClass(), getSampleXmlInputStream());
        final StringWriter writer = new StringWriter();
        CastorUtils.marshalWithTranslatedExceptions(obj, writer);
        _assertXmlEquals(getSampleXml(), writer.toString());
    }

    @Test
    public void marshalCastorUnmarshalCastor() throws Exception {
        final String xml = marshalToXmlWithCastor();
        final ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        final T obj = CastorUtils.unmarshal(getSampleClass(), is, false);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void unmarshalXmlAndCompareToJaxb() throws Exception {
        LogUtils.debugf(this, "xml: %s", getSampleXml());
        final T obj = JaxbUtils.unmarshal(getSampleClass(), new InputSource(getSampleXmlInputStream()), null);
        LogUtils.debugf(this, "Sample object: %s\n\nJAXB object: %s", getSampleObject(), obj);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void marshalCastorUnmarshalJaxb() throws Exception {
        final String xml = marshalToXmlWithCastor();
        final T obj = JaxbUtils.unmarshal(getSampleClass(), xml);
        LogUtils.debugf(this, "Generated Object: %s", obj);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void marshalJaxbUnmarshalCastor() throws Exception {
        final String xml = marshalToXmlWithJaxb();
        final T obj = CastorUtils.unmarshal(getSampleClass(), new ByteArrayInputStream(xml.getBytes()));
        LogUtils.debugf(this, "Generated Object: %s", obj);
        assertDepthEquals(getSampleObject(), obj);
    }

    @Test
    public void validateCastorObjectAgainstSchema() throws Exception {
        org.exolab.castor.xml.Unmarshaller unmarshaller = CastorUtils.getUnmarshaller(getSampleClass());
        unmarshaller.setValidation(true);
        @SuppressWarnings("unchecked")
        T obj = (T) unmarshaller.unmarshal(new InputSource(getSampleXmlInputStream()));
        assertNotNull(obj);
    }

    @Test
    public void validateJaxbXmlAgainstSchema() throws Exception {
        final String schemaFile = getSchemaFile();
        if (schemaFile == null) {
            LogUtils.warnf(this, "Skipping validation.");
            return;
        }
        LogUtils.debugf(this, "Validating against XSD: %s", schemaFile);
        javax.xml.bind.Unmarshaller unmarshaller = JaxbUtils.getUnmarshallerFor(getSampleClass(), null, true);
        final SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        final Schema schema = factory.newSchema(new StreamSource(schemaFile));
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(final ValidationEvent event) {
                LogUtils.warnf(this, event.getLinkedException(), "Received validation event: %s", event);
                return false;
            }
        });
        try {
            final InputSource inputSource = new InputSource(getSampleXmlInputStream());
            final XMLFilter filter = JaxbUtils.getXMLFilterForClass(getSampleClass());
            final SAXSource source = new SAXSource(filter, inputSource);
            @SuppressWarnings("unchecked")
            T obj = (T) unmarshaller.unmarshal(source);
            assertNotNull(obj);
        } finally {
            unmarshaller.setSchema(null);
        }
    }

    public static void assertXmlEquals(final String expectedXml, final String actualXml) {
        // ugly hack alert!
        final XmlTest<Object> test = new XmlTest<Object>(null, null, null) {
        };
        test._assertXmlEquals(expectedXml, actualXml);
    }

    protected void _assertXmlEquals(final String expectedXml, final String actualXml) {
        final List<Difference> differences = getDifferences(expectedXml, actualXml);
        if (differences.size() > 0) {
            LogUtils.debugf(this, "XML:\n\n%s\n\n...does not match XML:\n\n%s", expectedXml, actualXml);
        }
        assertEquals("number of XMLUnit differences between the expected xml and the actual xml should be 0", 0, differences.size());
    }

    public static void assertXpathDoesNotMatch(final String xml, final String expression) throws XPathExpressionException {
        assertXpathDoesNotMatch(null, xml, expression);
    }

    public static void assertXpathDoesNotMatch(final String description, final String xml, final String expression) throws XPathExpressionException {
        final NodeList nodes = xpathGetNodesMatching(xml, expression);
        assertTrue(description == null? ("Must get at least one node back from the query '" + expression + "'") : description, nodes == null || nodes.getLength() == 0);
    }

    public static void assertXpathMatches(final String xml, final String expression) throws XPathExpressionException {
        assertXpathMatches(null, xml, expression);
    }

    public static void assertXpathMatches(final String description, final String xml, final String expression) throws XPathExpressionException {
        final NodeList nodes = xpathGetNodesMatching(xml, expression);
        assertTrue(description == null? ("Must get at least one node back from the query '" + expression + "'") : description, nodes != null && nodes.getLength() != 0);
    }

    protected List<Difference> getDifferences(final String xmlA, final String xmlB) {
        DetailedDiff myDiff;
        try {
            myDiff = new DetailedDiff(XMLUnit.compareXML(xmlA, xmlB));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final List<Difference> retDifferences = new ArrayList<Difference>();
        @SuppressWarnings("unchecked")
        final List<Difference> allDifferences = myDiff.getAllDifferences();
        if (allDifferences.size() > 0) {
            DIFFERENCES: for (final Difference d : allDifferences) {
                final NodeDetail controlNodeDetail = d.getControlNodeDetail();
                final String control = controlNodeDetail.getValue();
                final NodeDetail testNodeDetail = d.getTestNodeDetail();
                final String test = testNodeDetail.getValue();

                if (d.getDescription().equals("namespace URI")) {
                    if (control != null && !"null".equals(control)) {
                        if (ignoreNamespace(control.toLowerCase())) {
                            LogUtils.tracef(this, "Ignoring %s: %s", d.getDescription(), d);
                            continue DIFFERENCES;
                        }
                    }
                    if (test != null && !"null".equals(test)) {
                        if (ignoreNamespace(test.toLowerCase())) {
                            LogUtils.tracef(this, "Ignoring %s: %s", d.getDescription(), d);
                            continue DIFFERENCES;
                        }
                    }
                } else if (d.getDescription().equals("namespace prefix")) {
                    if (control != null && !"null".equals(control)) {
                        if (ignorePrefix(control.toLowerCase())) {
                            LogUtils.tracef(this, "Ignoring %s: %s", d.getDescription(), d);
                            continue DIFFERENCES;
                        }
                    }
                    if (test != null && !"null".equals(test)) {
                        if (ignorePrefix(test.toLowerCase())) {
                            LogUtils.tracef(this, "Ignoring %s: %s", d.getDescription(), d);
                            continue DIFFERENCES;
                        }
                    }
                } else if (d.getDescription().equals("xsi:schemaLocation attribute")) {
                    LogUtils.debugf(this, "Schema location '%s' does not match.  Ignoring.", controlNodeDetail.getValue() == null? testNodeDetail.getValue() : controlNodeDetail.getValue());
                    continue DIFFERENCES;
                }

                if (ignoreDifference(d)) {
                    LogUtils.debugf(this, "ignoreDifference matched.  Ignoring difference: %s: %s", d.getDescription(), d);
                    continue DIFFERENCES;
                } else {
                    LogUtils.warnf(this, "Found difference: %s: %s", d.getDescription(), d);
                    retDifferences.add(d);
                }
            }
        }
        return retDifferences;
    }

    protected static NodeList xpathGetNodesMatching(final String xml, final String expression) throws XPathExpressionException {
        final XPath query = XPathFactory.newInstance().newXPath();
        StringReader sr = null;
        InputSource is = null;
        NodeList nodes = null;
        try {
            sr = new StringReader(xml);
            is = new InputSource(sr);
            nodes = (NodeList)query.evaluate(expression, is, XPathConstants.NODESET);
        } finally {
            sr.close();
            IOUtils.closeQuietly(sr);
        }
        return nodes;
    }

    private void assertDepthEquals(final Object expected, Object actual) {
        /*
        System.err.println("----------");
        System.err.println("expected: " + expected);
        System.err.println("actual:   " + actual);
        */

        if (expected == null && actual == null) {
            return;
        } else if (expected == null) {
            fail("expected was null but actual was not!");
        } else if (actual == null) {
            fail("actual was null but expected was not!");
        }

        if (expected.getClass().getName().startsWith("java") || actual.getClass().getName().startsWith("java")) {
            // java primitives, just do assertEquals
            if (expected instanceof Object[] || actual instanceof Object[]) {
                assertTrue(Arrays.equals((Object[])expected, (Object[])actual));
            } else {
                assertEquals(expected, actual);
            }
            return;
        }

        final BeanWrapper expectedWrapper = new BeanWrapperImpl(expected);
        final BeanWrapper actualWrapper   = new BeanWrapperImpl(actual);

        final Set<String> properties = new TreeSet<String>();
        for (final PropertyDescriptor descriptor : expectedWrapper.getPropertyDescriptors()) {
            properties.add(descriptor.getName());
        }
        for (final PropertyDescriptor descriptor : actualWrapper.getPropertyDescriptors()) {
            properties.add(descriptor.getName());
        }

        properties.remove("class");

        for (final String property : properties) {
            //System.err.println("property: " + property);
            final PropertyDescriptor expectedDescriptor = expectedWrapper.getPropertyDescriptor(property);
            final PropertyDescriptor actualDescriptor = actualWrapper.getPropertyDescriptor(property);
            
            if (expectedDescriptor != null && actualDescriptor != null) {
                // both have descriptors, so walk the sub-objects
                Object expectedValue = null;
                Object actualValue   = null;
                try {
                    expectedValue = expectedWrapper.getPropertyValue(property);
                } catch (final Exception e) {
                }
                try {
                    actualValue = actualWrapper.getPropertyValue(property);
                } catch (final Exception e) {
                }

                assertDepthEquals(expectedValue, actualValue);
            } else if (expectedDescriptor != null) {
                fail("Should have '" + property + "' property on actual object, but there was none!");
            } else if (actualDescriptor != null) {
                fail("Should have '" + property + "' property on expected object, but there was none!");
            }

        }

        if (expected instanceof Object[] || actual instanceof Object[]) {
            assertTrue(Arrays.equals((Object[])expected, (Object[])actual));
        } else {
            assertEquals(expected, actual);
        }
    }

}

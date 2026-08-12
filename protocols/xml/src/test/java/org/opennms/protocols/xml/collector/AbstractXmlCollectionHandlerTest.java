/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.protocols.xml.config.Request;
import org.w3c.dom.Document;

/**
 * The Test Class for AbstractXmlCollectionHandler.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AbstractXmlCollectionHandlerTest {

    /**
     * Test parse string.
     *
     * @throws Exception the exception
     */
    @Test
    public void testParseString() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("mynode.local");
        OnmsAssetRecord asset = new OnmsAssetRecord();
        asset.setSerialNumber("1001");
        node.setAssetRecord(asset);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("port", "80");
        String url = AbstractXmlCollectionHandler.parseString("URL", "http://{nodeLabel}:{parameter:port}/{ipAddress}/serial/{serialNumber}/{step}", node, "127.0.0.1", 300, parameters);
        Assert.assertEquals("http://mynode.local:80/127.0.0.1/serial/1001/300", url);
        String multiline = "<data>\n   <source label='{nodeLabel}'/>\n</data>";
        String xml = AbstractXmlCollectionHandler.parseString("Content", multiline, node, "127.0.0.1", 300, parameters);
        Assert.assertEquals("<data>\n   <source label='mynode.local'/>\n</data>", xml);

        String jsonContent = "{'test':{'key':'value','key2':0}}";
        String json = AbstractXmlCollectionHandler.parseString("Content", jsonContent, node, "127.0.0.1", 300, parameters);
        Assert.assertEquals(jsonContent, json);
    }

    /** NMS-20206: in-band XXE - an external general entity must not read a local file into the DOM. */
    @Test
    public void testInBandExternalEntityIsNotResolved() throws Exception {
        final File secret = File.createTempFile("nms20206-secret", ".txt");
        secret.deleteOnExit();
        Files.write(secret.toPath(), "TOP_SECRET_SENTINEL".getBytes(StandardCharsets.UTF_8));

        final String malicious =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE stats [ <!ENTITY xxe SYSTEM \"" + secret.toURI() + "\"> ]>\n" +
                "<stats><val>&xxe;</val></stats>";
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        try {
            final Document doc = handler.getXmlDocument(
                    new ByteArrayInputStream(malicious.getBytes(StandardCharsets.UTF_8)), null);
            final String text = doc.getElementsByTagName("val").item(0).getTextContent();
            Assert.assertFalse("External entity was resolved - XXE not blocked (leaked: " + text + ")",
                    text.contains("TOP_SECRET_SENTINEL"));
        } catch (Exception expected) {
            // Rejecting the entity outright is equally safe.
        }
    }

    /** NMS-20206: out-of-band XXE - an external parameter entity / external DTD must not be fetched. */
    @Test
    public void testOutOfBandParameterEntityIsNotResolved() throws Exception {
        final File secret = File.createTempFile("nms20206-oob", ".txt");
        secret.deleteOnExit();
        Files.write(secret.toPath(), "OOB_SECRET_SENTINEL".getBytes(StandardCharsets.UTF_8));

        // The nested parameter-entity trick is only well-formed in an EXTERNAL DTD, which is
        // exactly what the real attack fetches. An unhardened parser loads this and expands
        // &exfil; to the file contents; the hardening must prevent the external DTD load.
        final File dtd = File.createTempFile("nms20206-oob", ".dtd");
        dtd.deleteOnExit();
        Files.write(dtd.toPath(), (
                "<!ENTITY % file SYSTEM \"" + secret.toURI() + "\">\n" +
                "<!ENTITY % eval \"<!ENTITY exfil '%file;'>\">\n" +
                "%eval;\n").getBytes(StandardCharsets.UTF_8));

        final String malicious =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE stats [\n" +
                "  <!ENTITY % dtd SYSTEM \"" + dtd.toURI() + "\">\n" +
                "  %dtd;\n" +
                "]>\n" +
                "<stats><val>&exfil;</val></stats>";
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        try {
            final Document doc = handler.getXmlDocument(
                    new ByteArrayInputStream(malicious.getBytes(StandardCharsets.UTF_8)), null);
            final String text = doc.getElementsByTagName("val").item(0).getTextContent();
            Assert.assertFalse("External DTD was fetched - OOB XXE not blocked (leaked: " + text + ")",
                    text.contains("OOB_SECRET_SENTINEL"));
        } catch (Exception expected) {
            // Rejecting the undefined entity (because the external DTD was not loaded) is safe.
        }
    }

    /** NMS-20206: a benign DOCTYPE (e.g. pre-parse-html's &lt;!DOCTYPE html&gt;) must still parse. */
    @Test
    public void testBenignDoctypeStillParses() throws Exception {
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String withDoctype = "<!DOCTYPE html>\n<stats><val>ok</val></stats>";
        final Document doc = handler.getXmlDocument(
                new ByteArrayInputStream(withDoctype.getBytes(StandardCharsets.UTF_8)), null);
        Assert.assertNotNull(doc);
        Assert.assertEquals("ok", doc.getElementsByTagName("val").item(0).getTextContent());
    }

    /** NMS-20206: internal entities must still expand (read via XPath string(), as collection does). */
    @Test
    public void testInternalEntityIsExpanded() throws Exception {
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String xml =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE stats [ <!ENTITY ver \"1.2.3\"> ]>\n" +
                "<stats><val>v&ver;</val></stats>";
        final Document doc = handler.getXmlDocument(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null);
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final String value = (String) xpath.evaluate("string(/stats/val)", doc, XPathConstants.STRING);
        Assert.assertEquals("v1.2.3", value);
    }

    /** NMS-20206: XSLT collection must still work (TransformerFactory hardening is best-effort). */
    @Test
    public void testXsltTransformationStillWorks() throws Exception {
        // Stylesheet rewrites <val> to a fixed marker, so the assertion fails if the
        // transform is skipped (rather than a no-op identity transform that proves nothing).
        final File xslt = File.createTempFile("nms20206-xslt", ".xsl");
        xslt.deleteOnExit();
        final String stylesheet =
                "<?xml version=\"1.0\"?>\n" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "  <xsl:template match=\"/\">\n" +
                "    <stats><val>xslt-ran</val></stats>\n" +
                "  </xsl:template>\n" +
                "</xsl:stylesheet>";
        Files.write(xslt.toPath(), stylesheet.getBytes(StandardCharsets.UTF_8));

        final Request request = new Request();
        request.addParameter("xslt-source-file", xslt.getAbsolutePath());

        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String xml = "<stats><val>raw</val></stats>";
        final Document doc = handler.getXmlDocument(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), request);
        Assert.assertNotNull(doc);
        Assert.assertEquals("xslt-ran", doc.getElementsByTagName("val").item(0).getTextContent());
    }

    /** NMS-20206: XXE in the collected source must be blocked during XSLT too (Xalan ignores ACCESS_EXTERNAL_*). */
    @Test
    public void testXsltSourceExternalEntityIsNotResolved() throws Exception {
        final File secret = File.createTempFile("nms20206-xslt-src", ".txt");
        secret.deleteOnExit();
        Files.write(secret.toPath(), "XSLT_SRC_SENTINEL".getBytes(StandardCharsets.UTF_8));

        // Copy-through stylesheet: a resolved source entity would surface in the output.
        final File xslt = File.createTempFile("nms20206-xslt-src", ".xsl");
        xslt.deleteOnExit();
        Files.write(xslt.toPath(), (
                "<?xml version=\"1.0\"?>\n" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "  <xsl:template match=\"/\">\n" +
                "    <stats><val><xsl:value-of select=\"/stats/val\"/></val></stats>\n" +
                "  </xsl:template>\n" +
                "</xsl:stylesheet>").getBytes(StandardCharsets.UTF_8));

        final String malicious =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE stats [ <!ENTITY xxe SYSTEM \"" + secret.toURI() + "\"> ]>\n" +
                "<stats><val>&xxe;</val></stats>";

        final Request request = new Request();
        request.addParameter("xslt-source-file", xslt.getAbsolutePath());

        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        try {
            final Document doc = handler.getXmlDocument(
                    new ByteArrayInputStream(malicious.getBytes(StandardCharsets.UTF_8)), request);
            final String text = doc.getElementsByTagName("val").item(0).getTextContent();
            Assert.assertFalse("Source entity resolved during XSLT - XXE not blocked (leaked: " + text + ")",
                    text.contains("XSLT_SRC_SENTINEL"));
        } catch (Exception expected) {
            // Rejecting the entity outright is equally safe.
        }
    }

    /** NMS-20206: the XXE hardening must not break collection of normal, entity-free XML. */
    @Test
    public void testWellFormedXmlStillParses() throws Exception {
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String ok = "<stats><val>ok</val></stats>";
        final Document doc = handler.getXmlDocument(new ByteArrayInputStream(ok.getBytes(StandardCharsets.UTF_8)), null);
        Assert.assertNotNull(doc);
        Assert.assertEquals("ok", doc.getElementsByTagName("val").item(0).getTextContent());
    }

}

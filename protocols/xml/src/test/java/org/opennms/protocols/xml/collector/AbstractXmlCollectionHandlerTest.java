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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
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

    /**
     * NMS-20206: collected XML can be attacker-controlled. An external general entity that
     * points at a local file (the in-band XXE file-read vector) must never be resolved into
     * the parsed document. We assert the secret file content never reaches the DOM (the
     * parser may instead reject the reference outright - either outcome is safe).
     *
     * @throws Exception the exception
     */
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
            // The parser rejecting the disabled external entity is equally acceptable.
        }
    }

    /**
     * NMS-20206: the out-of-band vector uses an external parameter entity that pulls an
     * external DTD. With external-parameter-entities and load-external-dtd disabled, the
     * local file referenced through the parameter entity must not be read into the document.
     *
     * @throws Exception the exception
     */
    @Test
    public void testOutOfBandParameterEntityIsNotResolved() throws Exception {
        final File secret = File.createTempFile("nms20206-oob", ".txt");
        secret.deleteOnExit();
        Files.write(secret.toPath(), "OOB_SECRET_SENTINEL".getBytes(StandardCharsets.UTF_8));

        final String malicious =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE stats [\n" +
                "  <!ENTITY % file SYSTEM \"" + secret.toURI() + "\">\n" +
                "  <!ENTITY % eval \"<!ENTITY exfil '%file;'>\">\n" +
                "  %eval;\n" +
                "]>\n" +
                "<stats><val>&exfil;</val></stats>";
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        try {
            final Document doc = handler.getXmlDocument(
                    new ByteArrayInputStream(malicious.getBytes(StandardCharsets.UTF_8)), null);
            final String text = doc.getElementsByTagName("val").item(0).getTextContent();
            Assert.assertFalse("Parameter entity was resolved - OOB XXE not blocked (leaked: " + text + ")",
                    text.contains("OOB_SECRET_SENTINEL"));
        } catch (Exception expected) {
            // Rejecting the disabled parameter entity is equally acceptable.
        }
    }

    /**
     * NMS-20206: the hardening must not use disallow-doctype-decl, because the pre-parse-html
     * feature legitimately produces documents that begin with a benign, entity-free DOCTYPE
     * (e.g. &lt;!DOCTYPE html&gt;). Such documents must still parse.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBenignDoctypeStillParses() throws Exception {
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String withDoctype = "<!DOCTYPE html>\n<stats><val>ok</val></stats>";
        final Document doc = handler.getXmlDocument(
                new ByteArrayInputStream(withDoctype.getBytes(StandardCharsets.UTF_8)), null);
        Assert.assertNotNull(doc);
        Assert.assertEquals("ok", doc.getElementsByTagName("val").item(0).getTextContent());
    }

    /**
     * NMS-20206: the XXE hardening must not break collection of normal, entity-free XML.
     *
     * @throws Exception the exception
     */
    @Test
    public void testWellFormedXmlStillParses() throws Exception {
        final DefaultXmlCollectionHandler handler = new DefaultXmlCollectionHandler();
        final String ok = "<stats><val>ok</val></stats>";
        final Document doc = handler.getXmlDocument(new ByteArrayInputStream(ok.getBytes(StandardCharsets.UTF_8)), null);
        Assert.assertNotNull(doc);
        Assert.assertEquals("ok", doc.getElementsByTagName("val").item(0).getTextContent());
    }

}

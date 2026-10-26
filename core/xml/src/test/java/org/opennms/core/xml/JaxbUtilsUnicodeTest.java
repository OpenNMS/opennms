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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.xml.sax.InputSource;

/**
 * NMS-19814: supplementary characters (emoji and the like) survive marshalling, and
 * documents written by the old marshaller, which split them into two character
 * references, still load.
 */
public class JaxbUtilsUnicodeTest {

    @XmlRootElement(name = "probe")
    public static class Probe {
        @XmlAttribute(name = "pattern")
        public String pattern;
        @XmlValue
        public String value;
    }

    /** A root element shorter than the scanner's lookahead, so the closing tag is held back until the end. */
    @XmlRootElement(name = "p")
    public static class Short {
        @XmlValue
        public String value;
    }

    private static final String POO = "💩";          // U+1F4A9 PILE OF POO
    private static final String HALO = "😇";         // U+1F607 SMILING FACE WITH HALO
    private static final String TEXT = "café ☃ " + POO + " 17 firing";
    private static final String PATTERN = "/^one&two{;t|hree" + HALO + "$/";
    private static final String TEXT_OLD = "café " + POO;
    private static final String STORED = "<probe pattern=\"&#55357;&#56839;\">café &#55357;&#56489;</probe>";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static Probe probe() {
        final Probe probe = new Probe();
        probe.pattern = PATTERN;
        probe.value = TEXT;
        return probe;
    }

    @Test
    public void marshalToStringRoundTripsSupplementaryCharacters() {
        final String xml = JaxbUtils.marshal(probe());
        assertFalse("surrogate halves must not be written as character references: " + xml, xml.contains("&#55357;"));
        assertTrue(xml, xml.contains(TEXT));
        assertTrue(xml, xml.contains("/^one&amp;two{;t|hree" + HALO + "$/"));

        final Probe parsed = JaxbUtils.unmarshal(Probe.class, xml);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);
    }

    @Test
    public void marshalToWriterRoundTripsSupplementaryCharacters() {
        final StringWriter writer = new StringWriter();
        JaxbUtils.marshal(probe(), writer);
        final String xml = writer.toString();
        assertFalse(xml, xml.contains("&#55357;"));
        assertTrue(xml, xml.contains(TEXT));
        assertEquals(TEXT, JaxbUtils.unmarshal(Probe.class, new StringReader(xml)).value);
    }

    @Test
    public void xmlHandlerRoundTripsSupplementaryCharacters() {
        final XmlHandler<Probe> handler = new XmlHandler<>(Probe.class);
        final String xml = handler.marshal(probe());
        assertFalse(xml, xml.contains("&#55357;"));
        final Probe parsed = handler.unmarshal(xml);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);
        assertEquals(TEXT_OLD, handler.unmarshal(STORED).value);
    }

    @Test
    public void fileRoundTripsSupplementaryCharacters() throws Exception {
        final File file = folder.newFile("probe.xml");
        JaxbUtils.marshal(probe(), file);
        final String xml = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertFalse(xml, xml.contains("&#55357;"));
        assertEquals(TEXT, JaxbUtils.unmarshal(Probe.class, file).value);
    }

    @Test
    public void fileWrittenByTheOldMarshallerStillLoads() throws Exception {
        final File file = folder.newFile("old.xml");
        Files.write(file.toPath(), STORED.getBytes(StandardCharsets.UTF_8));
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, file);
        assertEquals(TEXT_OLD, parsed.value);
        assertEquals(HALO, parsed.pattern);
    }

    @Test
    public void everyUnmarshalOverloadRepairsOldReferences() {
        final byte[] bytes = STORED.getBytes(StandardCharsets.UTF_8);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, STORED).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new StringReader(STORED)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(bytes)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(bytes)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new InputSource(new ByteArrayInputStream(bytes))).value);
        assertEquals(HALO, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(bytes)).pattern);

        final byte[] declared = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + STORED).getBytes(StandardCharsets.UTF_8);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(declared)).value);
    }

    /** The parser still decodes the bytes by the document's own declaration. */
    @Test
    public void byteStreamInAnotherEncodingIsDecodedByTheParser() {
        final byte[] latin1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><probe>café &#55357;&#56489;</probe>"
                .getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(latin1)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(latin1)).value);

        final byte[] utf16 = ("<?xml version=\"1.0\" encoding=\"UTF-16\"?><probe>" + TEXT + "</probe>").getBytes(StandardCharsets.UTF_16);
        assertEquals(TEXT, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(utf16)).value);
        // these ideographs encode in UTF-16BE to the bytes of "&#55357;&#56489;"
        final String lookalike = "\u2623\u3535\u3335\u373b\u2623\u3536\u3438\u393b";
        final byte[] utf16Lookalike = ("<?xml version=\"1.0\" encoding=\"UTF-16\"?><probe>" + lookalike + "</probe>").getBytes(StandardCharsets.UTF_16);
        assertEquals(lookalike, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(utf16Lookalike)).value);
    }

    /** Inside a comment or CDATA section the references are literal text. */
    @Test
    public void commentsAndCdataAreNotRewritten() {
        final String xml = "<!-- &#55357;&#56489; --><probe pattern=\"&#55357;&#56839;\"><![CDATA[&#55357;&#56489;]]></probe>";
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, xml);
        assertEquals("&#55357;&#56489;", parsed.value);
        assertEquals(HALO, parsed.pattern);
        assertEquals("&#55357;&#56489;", JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).value);
    }

    /** A lone half cannot be represented in XML at all; U+FFFD keeps the document parseable. */
    @Test
    public void loneSurrogatesBecomeTheReplacementCharacter() {
        final Probe probe = new Probe();
        probe.value = "a\uD83Db";
        probe.pattern = "\uDCA9";
        final String xml = JaxbUtils.marshal(probe);
        assertFalse(xml, xml.contains("&#"));
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, xml);
        assertEquals("a�b", parsed.value);
        assertEquals("�", parsed.pattern);

        assertEquals("a�b", JaxbUtils.unmarshal(Probe.class, "<probe>a&#55357;b</probe>").value);
        assertEquals("a�" + POO, JaxbUtils.unmarshal(Probe.class, "<probe>a&#55357;&#55357;&#56489;</probe>").value);
    }

    /** The repair streams, so there is no size above which it stops working. */
    @Test
    public void largeInputIsRepaired() {
        final StringBuilder sb = new StringBuilder("<probe>");
        while (sb.length() < 9 * 1024 * 1024) {
            sb.append("lorem ipsum dolor sit amet ");
        }
        sb.append("&#55357;&#56489;</probe>");
        final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        final String value = JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(bytes)).value;
        assertTrue(value.endsWith(" " + POO));
        // the two references (16 chars) became one supplementary character (2 chars)
        assertEquals(sb.length() - "<probe></probe>".length() - 16 + 2, value.length());
    }

    /** The scanner only ever looks ten units ahead, so unterminated markup cannot make it quadratic. */
    @Test
    public void repairIsLinearOnHostileInput() {
        final StringBuilder hostile = new StringBuilder("<probe>");
        for (int i = 0; i < 200_000; i++) {
            hostile.append("&#5").append("<![CDATA").append("<!-");
        }
        hostile.append("</probe>");
        final long start = System.nanoTime();
        final String repaired = SurrogateCharacterReferences.repair(hostile.toString());
        final long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertEquals(hostile.toString(), repaired);
        assertTrue("took " + elapsedMillis + " ms", elapsedMillis < 5_000);
    }

    /** MOXy flushes the writer before the filter can let go of its lookahead, so the filter flushes again at the end. */
    @Test
    public void marshalToAnUnflushedWriterWritesTheClosingTag() {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final Writer writer = new BufferedWriter(new OutputStreamWriter(bytes, StandardCharsets.UTF_8));
        final Short s = new Short();
        s.value = POO;
        JaxbUtils.marshal(s, writer);
        final String xml = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(xml, xml.endsWith("</p>") || xml.endsWith("</p>\n"));
        assertEquals(POO, JaxbUtils.unmarshal(Short.class, xml).value);
    }

    /**
     * Bytes that are not valid in the declared or default encoding are rejected by the parser,
     * where the old default-charset reader replaced them silently.
     */
    @Test
    public void invalidBytesAreRejectedByTheParser() {
        final byte[] latin1Undeclared = "<probe>caf\u00e9</probe>".getBytes(StandardCharsets.ISO_8859_1);
        try {
            JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(latin1Undeclared));
            fail("the parser was expected to reject an invalid UTF-8 sequence");
        } catch (final DataAccessException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("UTF-8"));
        }
    }

    /** The parser closes the caller's stream, as it did before the repair was added. */
    @Test
    public void parserStillClosesTheStream() throws IOException {
        final boolean[] closed = new boolean[1];
        final FilterInputStream stream = new FilterInputStream(new ByteArrayInputStream(STORED.getBytes(StandardCharsets.UTF_8))) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new InputSource(stream)).value);
        assertTrue("closed by the parser", closed[0]);
    }
}

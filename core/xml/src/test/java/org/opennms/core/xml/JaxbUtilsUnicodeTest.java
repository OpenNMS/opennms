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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.InputSource;
import org.springframework.dao.DataAccessException;

/**
 * NMS-19814: characters outside the Basic Multilingual Plane (emoji, for one) are a
 * UTF-16 surrogate pair in a Java String. MOXy's Writer-based marshalling escapes each
 * half as its own character reference, which no XML parser accepts, so the output of
 * {@link JaxbUtils#marshal(Object)} could not be read back by {@link JaxbUtils#unmarshal}.
 */
public class JaxbUtilsUnicodeTest {

    @XmlRootElement(name = "probe")
    public static class Probe {
        @XmlAttribute(name = "pattern")
        public String pattern;

        @XmlValue
        public String value;
    }

    private static final String POO = "💩";          // U+1F4A9 PILE OF POO
    private static final String HALO = "😇";         // U+1F607 SMILING FACE WITH HALO
    private static final String TEXT = "café ☃ " + POO + " 17 firing";
    private static final String PATTERN = "/^one&two{;t|hree" + HALO + "$/";
    private static final String TEXT_OLD = "caf\u00e9 " + POO;

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

        final Probe parsed = JaxbUtils.unmarshal(Probe.class, xml);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);
    }

    @Test
    public void xmlHandlerRoundTripsSupplementaryCharacters() {
        final XmlHandler<Probe> handler = new XmlHandler<>(Probe.class);
        final String xml = handler.marshal(probe());
        assertFalse(xml, xml.contains("&#55357;"));

        final Probe parsed = handler.unmarshal(xml);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);
    }

    /** XML already persisted by the broken marshaller must still load. */
    @Test
    public void unmarshalRepairsSurrogateCharacterReferences() {
        final String stored = "<probe pattern=\"/^one&amp;two{;t|hree&#55357;&#56839;$/\">caf&#233; &#9731; &#55357;&#56489; 17 firing</probe>";
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, stored);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);

        final Probe viaHandler = new XmlHandler<>(Probe.class).unmarshal(stored);
        assertEquals(TEXT, viaHandler.value);
        assertEquals(PATTERN, viaHandler.pattern);
    }

    @Test
    public void repairHandlesHexReferencesAndLoneSurrogates() {
        assertEquals(POO, JaxbUtils.repairSurrogateCharacterReferences("&#xD83D;&#xDCA9;"));
        assertEquals(POO, JaxbUtils.repairSurrogateCharacterReferences("&#55357;&#xDCA9;"));
        // a lone half cannot be represented in XML at all; U+FFFD keeps the document loadable
        assertEquals("a�b", JaxbUtils.repairSurrogateCharacterReferences("a&#55357;b"));
        assertEquals("a�b", JaxbUtils.repairSurrogateCharacterReferences("a&#56489;b"));
        assertEquals("�" + POO, JaxbUtils.repairSurrogateCharacterReferences("&#55357;&#55357;&#56489;"));
        // ordinary references, text without references and malformed references are untouched
        final String plain = "<a b=\"&#xa;&#233;&#9731;\">&#x1F4A9; &#; &#xZZ; &#99999999;</a>";
        assertSame(plain, JaxbUtils.repairSurrogateCharacterReferences(plain));
        final String noRefs = "<a>nothing to see</a>";
        assertSame(noRefs, JaxbUtils.repairSurrogateCharacterReferences(noRefs));
    }

    @Test
    public void fileRoundTripsSupplementaryCharacters() throws Exception {
        final File file = folder.newFile("probe.xml");
        JaxbUtils.marshal(probe(), file);
        final String onDisk = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertFalse(onDisk, onDisk.contains("&#55357;"));

        final Probe parsed = JaxbUtils.unmarshal(Probe.class, file);
        assertEquals(TEXT, parsed.value);
        assertEquals(PATTERN, parsed.pattern);
    }

    /** A config file written by the old marshaller. */
    @Test
    public void fileWrittenByTheOldMarshallerStillLoads() throws Exception {
        final File file = folder.newFile("old.xml");
        Files.write(file.toPath(), "<probe pattern=\"&#55357;&#56839;\">caf\u00e9 &#55357;&#56489;</probe>".getBytes(StandardCharsets.UTF_8));
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, file);
        assertEquals("caf\u00e9 " + POO, parsed.value);
        assertEquals(HALO, parsed.pattern);
    }

    @Test
    public void readerStreamAndResourceOverloadsRepairOldReferences() {
        final String stored = "<probe pattern=\"&#55357;&#56839;\">caf\u00e9 &#55357;&#56489;</probe>";
        final byte[] bytes = stored.getBytes(StandardCharsets.UTF_8);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new StringReader(stored)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(bytes)).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(bytes)).value);
        assertEquals(HALO, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(bytes)).pattern);

        final byte[] declared = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + stored).getBytes(StandardCharsets.UTF_8);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(declared)).value);
    }

    /** The bytes of a document in another encoding are not rewritten as UTF-8. */
    @Test
    public void byteStreamDeclaringAnotherEncodingIsLeftAlone() {
        final byte[] latin1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><probe>caf\u00e9 &#55357;&#56489;</probe>"
                .getBytes(StandardCharsets.ISO_8859_1);
        try {
            JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(latin1));
            fail("the invalid references were expected to reach the parser");
        } catch (final DataAccessException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("55357"));
        }
        final byte[] clean = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><probe>caf\u00e9</probe>".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("caf\u00e9", JaxbUtils.unmarshal(Probe.class, new ByteArrayResource(clean)).value);
    }

    /** A lone half cannot be represented in XML at all; U+FFFD keeps the output parseable. */
    @Test
    public void marshalReplacesLoneSurrogates() {
        final Probe probe = new Probe();
        probe.value = "a\uD83Dbcd";
        probe.pattern = "abc\uD83D";
        final String xml = JaxbUtils.marshal(probe);
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, xml);
        assertEquals("a\uFFFDbcd", parsed.value);
        assertEquals("abc\uFFFD", parsed.pattern);
        probe.value = "x\uDCA9y";
        assertEquals("x\uFFFDy", JaxbUtils.unmarshal(Probe.class, JaxbUtils.marshal(probe)).value);
    }

    @Test(timeout = 5000)
    public void repairIsLinearOnHostileInput() {
        final StringBuilder hostile = new StringBuilder("&#");
        for (int i = 0; i < 300_000; i++) {
            hostile.append("<!--<![CDATA[<?&#5");
        }
        final String input = hostile.toString();
        assertSame(input, JaxbUtils.repairSurrogateCharacterReferences(input));
        final StringBuilder refs = new StringBuilder();
        for (int i = 0; i < 300_000; i++) {
            refs.append("&#55357;&#56489;&#55357;x&#9731;");
        }
        final String repaired = JaxbUtils.repairSurrogateCharacterReferences(refs.toString());
        assertEquals(300_000 * (POO.length() + 1 + 1 + "&#9731;".length()), repaired.length());
    }

    @Test
    public void inputsAboveTheRepairLimitStillParse() {
        final StringBuilder big = new StringBuilder("<probe pattern=\"p\">");
        final int length = 9 * 1024 * 1024;
        for (int i = 0; i < length; i++) {
            big.append('a');
        }
        big.append("</probe>");
        final String xml = big.toString();
        assertEquals(length, JaxbUtils.unmarshal(Probe.class, new StringReader(xml)).value.length());
        assertEquals(length, JaxbUtils.unmarshal(Probe.class, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).value.length());
    }

    /** The parser used to close the stream it was handed; buffering must not leak it. */
    @Test
    public void bufferedStreamsAreClosed() {
        final String stored = "<probe>caf\u00e9 &#55357;&#56489;</probe>";
        final boolean[] closed = new boolean[2];
        final FilterReader reader = new FilterReader(new StringReader(stored)) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        final FilterInputStream stream = new FilterInputStream(new ByteArrayInputStream(stored.getBytes(StandardCharsets.UTF_8))) {
            @Override
            public void close() throws IOException {
                closed[1] = true;
                super.close();
            }
        };
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, reader).value);
        assertEquals(TEXT_OLD, JaxbUtils.unmarshal(Probe.class, new InputSource(stream)).value);
        assertTrue("reader closed", closed[0]);
        assertTrue("stream closed", closed[1]);
    }

    /** UTF-16 bytes can spell "&#55357;" by accident; they must not be treated as UTF-8. */
    @Test
    public void utf16ByteStreamsAreLeftAlone() {
        final String lookalike = "\u2326\u3535\u3733\u3b37";          // UTF-16LE bytes of this read as "&#55377;"
        final String document = "<?xml version=\"1.0\"?><probe pattern=\"" + POO + "\">" + lookalike + "</probe>";
        for (final java.nio.charset.Charset charset : new java.nio.charset.Charset[] {
                StandardCharsets.UTF_16LE, StandardCharsets.UTF_16BE, StandardCharsets.UTF_16 }) {
            final byte[] bytes = document.getBytes(charset);
            final Probe parsed = JaxbUtils.unmarshal(Probe.class, new InputSource(new ByteArrayInputStream(bytes)));
            assertEquals(charset.name(), lookalike, parsed.value);
            assertEquals(charset.name(), POO, parsed.pattern);
        }
    }

    @Test
    public void inputSourceEncodingIsHonoured() {
        final InputSource latin1 = new InputSource(new ByteArrayInputStream("<probe>caf\u00e9</probe>".getBytes(StandardCharsets.ISO_8859_1)));
        latin1.setEncoding("ISO-8859-1");
        assertEquals("caf\u00e9", JaxbUtils.unmarshal(Probe.class, latin1).value);

        final InputSource withRefs = new InputSource(new ByteArrayInputStream("<probe>caf\u00e9 &#55357;&#56489;</probe>".getBytes(StandardCharsets.ISO_8859_1)));
        withRefs.setEncoding("ISO-8859-1");
        try {
            JaxbUtils.unmarshal(Probe.class, withRefs);
            fail("the invalid references were expected to reach the parser");
        } catch (final DataAccessException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("55357"));
        }
    }

    @Test
    public void inputsAtTheRepairLimitAreRepaired() {
        final String prefix = "<probe>";
        final String suffix = "&#55357;&#56489;</probe>";
        final StringBuilder exact = new StringBuilder(prefix);
        for (int i = exact.length(); i < 8 * 1024 * 1024 - suffix.length(); i++) {
            exact.append('a');
        }
        exact.append(suffix);
        assertEquals(8 * 1024 * 1024, exact.length());
        final Probe parsed = JaxbUtils.unmarshal(Probe.class, new StringReader(exact.toString()));
        assertTrue(parsed.value.endsWith(POO));
    }
}

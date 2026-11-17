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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class SurrogateCharacterReferencesTest {

    private static final String POO = "💩";   // U+1F4A9

    @Test
    public void pairsBecomeTheCharacter() {
        assertEquals(POO, SurrogateCharacterReferences.repair("&#55357;&#56489;"));
        assertEquals(POO, SurrogateCharacterReferences.repair("&#xD83D;&#xDCA9;"));
        assertEquals(POO, SurrogateCharacterReferences.repair("&#55357;&#xdca9;"));
        assertEquals("a" + POO + "b" + POO, SurrogateCharacterReferences.repair("a&#55357;&#56489;b&#55357;&#56489;"));
    }

    @Test
    public void unpairedHalvesBecomeTheReplacementCharacter() {
        assertEquals("a�b", SurrogateCharacterReferences.repair("a&#55357;b"));
        assertEquals("a�b", SurrogateCharacterReferences.repair("a&#56489;b"));
        assertEquals("�" + POO, SurrogateCharacterReferences.repair("&#55357;&#55357;&#56489;"));
        assertEquals("�", SurrogateCharacterReferences.repair("&#55357;"));
        // only adjacent references pair up
        assertEquals("� �", SurrogateCharacterReferences.repair("&#55357; &#56489;"));
        assertEquals("�<b/>�", SurrogateCharacterReferences.repair("&#55357;<b/>&#56489;"));
    }

    @Test
    public void otherReferencesAndTextAreUntouched() {
        // the XML production allows ASCII digits only, so fullwidth digits are not a reference
        final String plain = "<a b=\"&#60;&#x3C;&amp;\">&#128169; &#1114111; &# &#; &#x; &#xZZ; & &#5535 &#\uff15\uff15\uff13\uff15\uff17;&#00055357;</a>";
        assertEquals(plain, SurrogateCharacterReferences.repair(plain));
        final String noRefs = "<a>" + POO + "</a>";
        assertSame(noRefs, SurrogateCharacterReferences.repair(noRefs));
    }

    @Test
    public void commentsCdataAndProcessingInstructionsAreLiteral() {
        final String xml = "<?pi &#55357;&#56489;?><!-- &#55357;&#56489; --><a><![CDATA[&#55357;&#56489;]]>&#55357;&#56489;<!---->&#55357;</a>";
        assertEquals("<?pi &#55357;&#56489;?><!-- &#55357;&#56489; --><a><![CDATA[&#55357;&#56489;]]>" + POO + "<!---->�</a>",
                SurrogateCharacterReferences.repair(xml));
        // a pending high half is not carried into markup
        assertEquals("�<!-- &#56489; -->", SurrogateCharacterReferences.repair("&#55357;<!-- &#56489; -->"));
        // unterminated markup swallows the rest, as it would for the parser
        final String open = "<a><![CDATA[&#55357;&#56489;";
        assertEquals(open, SurrogateCharacterReferences.repair(open));
    }

    /** Every split of the input across writes gives the same output. */
    @Test
    public void writerRepairsAcrossWriteBoundaries() throws IOException {
        final String xml = "<a b=\"&#55357;&#56839;\"><![CDATA[&#55357;]]>x&#55357;&#56489;&#55357;</a>";
        final String expected = SurrogateCharacterReferences.repair(xml);
        for (int split = 0; split <= xml.length(); split++) {
            final StringWriter target = new StringWriter();
            final SurrogateCharacterReferences.RepairingWriter writer = SurrogateCharacterReferences.repairing(target);
            writer.write(xml.substring(0, split));
            writer.flush();
            writer.write(xml.substring(split));
            writer.finish();
            assertEquals("split at " + split, expected, target.toString());
        }
    }

    @Test
    public void readerRepairsAcrossReadBoundaries() throws IOException {
        final String xml = "<a b=\"&#55357;&#56839;\"><![CDATA[&#55357;]]>x&#55357;&#56489;&#55357;</a>";
        final String expected = SurrogateCharacterReferences.repair(xml);
        final Reader reader = SurrogateCharacterReferences.repairing(new OneCharReader(xml));
        final StringBuilder out = new StringBuilder();
        final char[] buf = new char[3];
        int n;
        while ((n = reader.read(buf, 0, buf.length)) >= 0) {
            out.append(buf, 0, n);
        }
        assertEquals(expected, out.toString());
    }

    /** Byte streams get a single numeric reference, which is valid in any encoding. */
    @Test
    public void byteStreamEmitsOneReferencePerPair() throws IOException {
        final byte[] input = "<a b=\"&#55357;&#56839;\">café &#55357;&#56489; &#56489;</a>".getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals("<a b=\"&#128519;\">café &#128169; &#65533;</a>".getBytes(StandardCharsets.ISO_8859_1), readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(input))));
        // single-byte reads
        final InputStream stream = SurrogateCharacterReferences.repairing(new ByteArrayInputStream(input));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = stream.read()) >= 0) {
            out.write(b);
        }
        assertArrayEquals("<a b=\"&#128519;\">café &#128169; &#65533;</a>".getBytes(StandardCharsets.ISO_8859_1), out.toByteArray());
    }

    /** The filter hands decided output on in chunks; an explicit finish flushes the target. */
    @Test
    public void writerFinishFlushesTheTarget() throws IOException {
        final StringWriter target = new StringWriter();
        final BufferedWriter buffered = new BufferedWriter(target);
        final SurrogateCharacterReferences.RepairingWriter writer = SurrogateCharacterReferences.repairing(buffered);
        for (final char c : "<p>&#55357;&#56489;</p>".toCharArray()) {
            writer.write(c);
        }
        writer.finish();
        assertEquals("<p>" + POO + "</p>", target.toString());
    }

    /** In UTF-16 the bytes of U+2623 U+3535 U+3335 U+373B U+2623 U+3536 U+3438 U+393B read as the ASCII of a pair of references. */
    @Test
    public void wideEncodingsAndShortStreamsPassThrough() throws IOException {
        final String lookalike = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><a>\u2623\u3535\u3335\u373b\u2623\u3536\u3438\u393b</a>";
        for (final byte[] input : new byte[][] {
                lookalike.getBytes(StandardCharsets.UTF_16),
                lookalike.getBytes(StandardCharsets.UTF_16BE),
                lookalike.replace("UTF-16", "UTF-16LE").getBytes(StandardCharsets.UTF_16LE),
                "<a>&#55357;&#56489;</a>".getBytes(StandardCharsets.UTF_16),
                "<a>&#55357;&#56489;</a>".getBytes(StandardCharsets.UTF_16LE),
                "<a>&#55357;&#56489;</a>".getBytes(StandardCharsets.UTF_16BE),
                "<a/>".getBytes(StandardCharsets.UTF_8),
                "".getBytes(StandardCharsets.UTF_8),
                "&#5".getBytes(StandardCharsets.UTF_8),
        }) {
            assertArrayEquals(input, readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(input))));
        }
    }

    /**
     * Literals in the document type declaration may hold markup-like text; a section opener
     * in there must not swallow the real CDATA section that follows.
     */
    @Test
    public void documentTypeDeclarationIsLiteral() throws IOException {
        for (final String literal : new String[] {
                "<!DOCTYPE r [<!ENTITY e \"<!--\">]><r><![CDATA[-->&#55357;&#56489;]]></r>",
                "<!DOCTYPE r [<!ENTITY e '<?'>]><r><![CDATA[?>&#55357;&#56489;]]></r>",
                "<!DOCTYPE r [<!ENTITY e \"&#55357;&#56489;\">]><r/>",
        }) {
            assertSame(literal, SurrogateCharacterReferences.repair(literal));
            assertEquals(literal, readAll(SurrogateCharacterReferences.repairing(new OneCharReader(literal))));
            assertArrayEquals(literal.getBytes(StandardCharsets.UTF_8), readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(literal.getBytes(StandardCharsets.UTF_8)))));
        }
        // the declaration ends at the first '>' outside the subset, a literal or a comment
        assertEquals("<!DOCTYPE r [<!-- ]> --><!ENTITY e '>'><?pi ]>?>]><r>" + POO + "</r>",
                SurrogateCharacterReferences.repair("<!DOCTYPE r [<!-- ]> --><!ENTITY e '>'><?pi ]>?>]><r>&#55357;&#56489;</r>"));
        assertEquals("<!DOCTYPE r SYSTEM \"r.dtd\"><r>" + POO + "</r>",
                SurrogateCharacterReferences.repair("<!DOCTYPE r SYSTEM \"r.dtd\"><r>&#55357;&#56489;</r>"));
    }

    /**
     * ISO-2022-JP switches to two-byte JIS characters in the ASCII range: U+0393 U+4E80
     * U+6982 U+5144 encode to the bytes of {@code &#55357;}. Only an encoding in which every
     * ASCII byte is that character is scanned.
     */
    @Test
    public void nonAsciiCompatibleEncodingsPassThrough() throws IOException {
        final Charset iso2022jp = Charset.forName("ISO-2022-JP");
        final String jis = new String("\u001b$B&#55357;&#56489;\u001b(B".getBytes(StandardCharsets.US_ASCII), iso2022jp);
        final byte[] lookalike = ("<?xml version=\"1.0\" encoding=\"ISO-2022-JP\"?><a>" + jis + "</a>").getBytes(iso2022jp);
        assertTrue(new String(lookalike, StandardCharsets.US_ASCII).contains("&#55357;&#56489;"));
        assertArrayEquals(lookalike, readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(lookalike))));

        final String pair = "<a>&#55357;&#56489;</a>";
        for (final String encoding : new String[] { "Shift_JIS", "GB18030", "ISO-2022-KR", "no-such-encoding" }) {
            final byte[] input = ("<?xml version='1.0' encoding='" + encoding + "'?>" + pair).getBytes(StandardCharsets.US_ASCII);
            assertArrayEquals(encoding, input, readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(input))));
        }
        for (final String encoding : new String[] { "UTF-8", "utf-8", "ISO-8859-1", "latin1", "windows-1252", "US-ASCII" }) {
            final byte[] input = ("\ufeff<?xml version=\"1.0\"  encoding = \"" + encoding + "\" ?>" + pair).getBytes(StandardCharsets.UTF_8);
            assertEquals(encoding, new String(input, StandardCharsets.UTF_8).replace(pair, "<a>&#128169;</a>"),
                    new String(readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(input))), StandardCharsets.UTF_8));
        }
        // EBCDIC does not even spell the markup in ASCII
        final byte[] ebcdic = "<a>&#55357;&#56489;</a>".getBytes(Charset.forName("IBM037"));
        assertArrayEquals(ebcdic, readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(ebcdic))));
    }

    /** A document with ordinary references is scanned but never copied; a string comes back as the same instance. */
    @Test
    public void unchangedTextIsNotCopied() throws IOException {
        final String xml = "<?xml version=\"1.0\"?><a b=\"&#10;\">&#x1F4A9; &amp; <![CDATA[&#55357;]]> " + POO + "</a>";
        assertSame(xml, SurrogateCharacterReferences.repair(xml));
        assertEquals(xml, readAll(SurrogateCharacterReferences.repairing(new StringReader(xml))));
        assertArrayEquals(xml.getBytes(StandardCharsets.UTF_8), readAll(SurrogateCharacterReferences.repairing(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))));
    }

    /** One large write is drained in a single pass, not chunk by chunk with a shrinking buffer. */
    @Test
    public void oneLargeWriteStaysLinear() throws IOException {
        final StringBuilder sb = new StringBuilder(32 * 1024 * 1024);
        while (sb.length() < 32 * 1024 * 1024) {
            sb.append("lorem ipsum ");
        }
        final Writer sink = new Writer() {
            @Override public void write(final char[] cbuf, final int off, final int len) { }
            @Override public void flush() { }
            @Override public void close() { }
        };
        final long start = System.nanoTime();
        final SurrogateCharacterReferences.RepairingWriter writer = SurrogateCharacterReferences.repairing(sink);
        writer.write(sb.toString());
        writer.finish();
        final long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue("took " + elapsedMillis + " ms", elapsedMillis < 2_000);
    }

    private static String readAll(final Reader reader) throws IOException {
        final StringWriter out = new StringWriter();
        final char[] buf = new char[7];
        int n;
        while ((n = reader.read(buf, 0, buf.length)) >= 0) {
            out.write(buf, 0, n);
        }
        return out.toString();
    }

    private static byte[] readAll(final InputStream stream) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[7];
        int n;
        while ((n = stream.read(buf, 0, buf.length)) >= 0) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static final class OneCharReader extends StringReader {
        OneCharReader(final String s) {
            super(s);
        }

        @Override
        public int read(final char[] cbuf, final int off, final int len) throws IOException {
            return super.read(cbuf, off, Math.min(len, 1));
        }
    }
}

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

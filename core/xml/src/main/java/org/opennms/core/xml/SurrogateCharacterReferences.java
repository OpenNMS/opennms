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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repairs surrogate pairs that were written as two character references, such as
 * {@code &#55357;&#56489;} for U+1F4A9. MOXy's Writer output produces them (NMS-19814),
 * and no XML parser accepts them. Adjacent references for a high and a low half become
 * the character they encode; an unpaired half becomes U+FFFD. References inside comments,
 * CDATA sections and processing instructions are literal text and are left alone.
 *
 * The filters stream with at most ten units of lookahead, so they add no buffering and
 * run in linear time on any input.
 */
public final class SurrogateCharacterReferences {
    private static final Logger LOG = LoggerFactory.getLogger(SurrogateCharacterReferences.class);

    /** Longest unit of markup the scanner has to see at once: {@code &#1114111;} or {@code <![CDATA[}. */
    private static final int LOOKAHEAD = 10;

    private SurrogateCharacterReferences() {
    }

    /** Repairs a complete document or fragment. Returns the argument itself if there is nothing to do. */
    public static String repair(final String xml) {
        if (xml == null || xml.indexOf("&#") < 0) {
            return xml;
        }
        final Scanner scanner = new Scanner(false);
        scanner.accept(xml, 0, xml.length());
        scanner.finish();
        return scanner.out.toString();
    }

    /**
     * Repairs what is written through the returned writer. Repaired characters are written
     * as characters. Closing the writer closes the target; {@link RepairingWriter#finish()}
     * writes the lookahead out without closing.
     */
    public static RepairingWriter repairing(final Writer out) {
        return new RepairingWriter(out);
    }

    /** Repairs what is read through the returned reader. Repaired characters are read as characters. */
    public static Reader repairing(final Reader in) {
        return new RepairingReader(in);
    }

    /**
     * Repairs what is read through the returned stream. Repaired characters are read as a
     * single numeric reference, which is valid in every encoding, so the parser still honours
     * the document's own encoding declaration. A stream that starts like UTF-16 or UTF-32 is
     * passed through untouched.
     */
    public static InputStream repairing(final InputStream in) {
        return new RepairingInputStream(in);
    }

    /**
     * Incremental scanner over chars, or over the bytes of an ASCII-compatible encoding
     * widened to chars: everything it inspects is ASCII, and other units pass through.
     */
    static final class Scanner {
        private enum Mode { TEXT, COMMENT, CDATA, PI }

        /** Emit repaired code points as numeric references (byte streams) instead of characters. */
        private final boolean references;
        private final StringBuilder in = new StringBuilder(LOOKAHEAD * 2);
        private final StringBuilder out = new StringBuilder();
        private Mode mode = Mode.TEXT;
        private int pendingHigh = -1;
        private int unpaired = 0;

        Scanner(final boolean references) {
            this.references = references;
        }

        void accept(final CharSequence chars, final int from, final int to) {
            int i = from;
            while (i < to) {
                if (mode == Mode.TEXT && in.length() == 0 && pendingHigh < 0) {
                    // fast path: copy a run of plain text without going through the lookahead
                    int run = i;
                    while (run < to && chars.charAt(run) != '&' && chars.charAt(run) != '<') {
                        run++;
                    }
                    if (run > i) {
                        out.append(chars, i, run);
                        i = run;
                        continue;
                    }
                }
                in.append(chars.charAt(i++));
                drain(false);
            }
        }

        void accept(final char[] chars, final int off, final int len) {
            accept(CharBuffer.wrap(chars, off, len), 0, len);
        }

        void finish() {
            drain(true);
            flushPendingLone();
            if (unpaired > 0) {
                LOG.warn("Replaced {} unpaired surrogate character reference(s) with U+FFFD", unpaired);
                unpaired = 0;
            }
        }

        boolean hasOutput() {
            return out.length() > 0;
        }

        int take(final char[] buf, final int off, final int len) {
            final int n = Math.min(len, out.length());
            out.getChars(0, n, buf, off);
            out.delete(0, n);
            return n;
        }

        private void drain(final boolean eof) {
            while (in.length() > 0 && step(eof)) {
                // keep going
            }
        }

        /** Decides on the start of the lookahead; false means more input is needed first. */
        private boolean step(final boolean eof) {
            switch (mode) {
            case COMMENT:
                return closing("-->", eof);
            case CDATA:
                return closing("]]>", eof);
            case PI:
                return closing("?>", eof);
            default:
                break;
            }
            final char c = in.charAt(0);
            if (c != '&' && c != '<') {
                flushPendingLone();
                shift(1);
                return true;
            }
            if (in.length() < LOOKAHEAD && !eof) {
                return false;
            }
            if (c == '<') {
                flushPendingLone();
                if (startsWith("<!--")) {
                    mode = Mode.COMMENT;
                    shift(4);
                } else if (startsWith("<![CDATA[")) {
                    mode = Mode.CDATA;
                    shift(9);
                } else if (startsWith("<?")) {
                    mode = Mode.PI;
                    shift(2);
                } else {
                    shift(1);
                }
                return true;
            }
            final int end = referenceEnd();
            if (end < 0) {
                flushPendingLone();
                shift(1);
                return true;
            }
            final int value = referenceValue(end);
            if (value >= Character.MIN_HIGH_SURROGATE && value <= Character.MAX_HIGH_SURROGATE) {
                flushPendingLone();
                pendingHigh = value;
                in.delete(0, end);
            } else if (value >= Character.MIN_LOW_SURROGATE && value <= Character.MAX_LOW_SURROGATE) {
                if (pendingHigh >= 0) {
                    emit(Character.toCodePoint((char) pendingHigh, (char) value));
                    pendingHigh = -1;
                } else {
                    emitLone();
                }
                in.delete(0, end);
            } else {
                flushPendingLone();
                shift(end);
            }
            return true;
        }

        private boolean closing(final String marker, final boolean eof) {
            if (in.length() < marker.length() && !eof) {
                return false;
            }
            if (startsWith(marker)) {
                mode = Mode.TEXT;
                shift(marker.length());
            } else {
                shift(1);
            }
            return true;
        }

        /** End of the numeric reference at the start of the lookahead, or -1. */
        private int referenceEnd() {
            if (in.length() < 4 || in.charAt(1) != '#') {
                return -1;
            }
            int i = 2;
            final boolean hex = in.charAt(2) == 'x' || in.charAt(2) == 'X';
            if (hex) {
                i++;
            }
            final int digits = i;
            while (i < in.length() && i - digits < (hex ? 6 : 7) && Character.digit(in.charAt(i), hex ? 16 : 10) >= 0) {
                i++;
            }
            if (i == digits || i >= in.length() || in.charAt(i) != ';') {
                return -1;
            }
            return i + 1;
        }

        private int referenceValue(final int end) {
            final boolean hex = in.charAt(2) == 'x' || in.charAt(2) == 'X';
            return Integer.parseInt(in.substring(hex ? 3 : 2, end - 1), hex ? 16 : 10);
        }

        private boolean startsWith(final String s) {
            return in.length() >= s.length() && in.indexOf(s) == 0;
        }

        private void shift(final int n) {
            out.append(in, 0, n);
            in.delete(0, n);
        }

        private void emit(final int codePoint) {
            if (references) {
                out.append("&#").append(codePoint).append(';');
            } else {
                out.appendCodePoint(codePoint);
            }
        }

        private void emitLone() {
            unpaired++;
            emit(0xFFFD);
        }

        private void flushPendingLone() {
            if (pendingHigh >= 0) {
                pendingHigh = -1;
                emitLone();
            }
        }
    }

    public static final class RepairingWriter extends Writer {
        private final Writer out;
        private final Scanner scanner = new Scanner(false);
        private final char[] buffer = new char[8192];

        RepairingWriter(final Writer out) {
            this.out = Objects.requireNonNull(out);
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            scanner.accept(cbuf, off, len);
            forward();
        }

        @Override
        public void write(final String str, final int off, final int len) throws IOException {
            scanner.accept(str, off, off + len);
            forward();
        }

        @Override
        public void flush() throws IOException {
            forward();
            out.flush();
        }

        /** Writes the lookahead out, closing any pending pair, without closing the target. */
        public void finish() throws IOException {
            scanner.finish();
            forward();
        }

        @Override
        public void close() throws IOException {
            finish();
            out.close();
        }

        private void forward() throws IOException {
            while (scanner.hasOutput()) {
                out.write(buffer, 0, scanner.take(buffer, 0, buffer.length));
            }
        }
    }

    private static final class RepairingReader extends Reader {
        private final Reader in;
        private final Scanner scanner = new Scanner(false);
        private final char[] buffer = new char[8192];
        private boolean eof;

        RepairingReader(final Reader in) {
            this.in = Objects.requireNonNull(in);
        }

        @Override
        public int read(final char[] cbuf, final int off, final int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            while (!scanner.hasOutput()) {
                if (eof) {
                    return -1;
                }
                final int n = in.read(buffer);
                if (n < 0) {
                    eof = true;
                    scanner.finish();
                } else {
                    scanner.accept(buffer, 0, n);
                }
            }
            return scanner.take(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private static final class RepairingInputStream extends InputStream {
        private final InputStream in;
        private final Scanner scanner = new Scanner(true);
        private final byte[] buffer = new byte[8192];
        private final char[] chars = new char[8192];
        /** Set once the first bytes have been seen: the raw stream (with those bytes put back) or null to repair. */
        private InputStream passthrough;
        private boolean sniffed;
        private boolean eof;

        RepairingInputStream(final InputStream in) {
            this.in = Objects.requireNonNull(in);
        }

        @Override
        public int read() throws IOException {
            final byte[] one = new byte[1];
            final int n = read(one, 0, 1);
            return n < 0 ? -1 : one[0] & 0xFF;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            if (!sniffed) {
                sniff();
            }
            if (passthrough != null) {
                return passthrough.read(b, off, len);
            }
            while (!scanner.hasOutput()) {
                if (eof) {
                    return -1;
                }
                final int n = in.read(buffer);
                if (n < 0) {
                    eof = true;
                    scanner.finish();
                } else {
                    feed(buffer, n);
                }
            }
            final int n = scanner.take(chars, 0, Math.min(len, chars.length));
            for (int i = 0; i < n; i++) {
                b[off + i] = (byte) chars[i];
            }
            return n;
        }

        /** A NUL or a byte-order mark among the first bytes means UTF-16 or UTF-32, which the scanner cannot read. */
        private void sniff() throws IOException {
            sniffed = true;
            int n = 0;
            while (n < 4) {
                final int r = in.read(buffer, n, 4 - n);
                if (r < 0) {
                    break;
                }
                n += r;
            }
            boolean wide = n >= 2 && ((buffer[0] == (byte) 0xFE && buffer[1] == (byte) 0xFF) || (buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFE));
            for (int i = 0; i < n; i++) {
                wide |= buffer[i] == 0;
            }
            if (wide) {
                final byte[] head = new byte[n];
                System.arraycopy(buffer, 0, head, 0, n);
                passthrough = new SequenceInputStream(new ByteArrayInputStream(head), in);
                return;
            }
            feed(buffer, n);
            if (n < 4) {
                eof = true;
                scanner.finish();
            }
        }

        private void feed(final byte[] bytes, final int n) {
            for (int i = 0; i < n; i++) {
                chars[i] = (char) (bytes[i] & 0xFF);
            }
            scanner.accept(chars, 0, n);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}

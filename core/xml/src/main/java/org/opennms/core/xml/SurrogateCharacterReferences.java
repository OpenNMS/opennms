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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repairs surrogate pairs that were written as two character references, such as
 * {@code &#55357;&#56489;} for U+1F4A9. MOXy's Writer output produces them (NMS-19814),
 * and no XML parser accepts them. Adjacent references for a high and a low half become
 * the character they encode; an unpaired half becomes U+FFFD. References inside comments,
 * CDATA sections and processing instructions are literal text and are left alone.
 *
 * Only references of up to seven decimal or six hexadecimal ASCII digits are recognised,
 * which covers everything the marshaller ever wrote. The filters stream with at most ten
 * units of lookahead and run in linear time on any input; the writer hands decided text
 * on in chunks of {@link #CHUNK} characters.
 */
public final class SurrogateCharacterReferences {
    private static final Logger LOG = LoggerFactory.getLogger(SurrogateCharacterReferences.class);

    /** Longest unit of markup the scanner has to see at once: {@code &#1114111;} or {@code <![CDATA[}. */
    private static final int LOOKAHEAD = 10;

    /** Size of the filters' transfer buffers. */
    private static final int CHUNK = 8192;

    /** Unpaired halves are data loss, but one bad document replayed over IPC must not flood the log. */
    private static final long WARN_INTERVAL_MILLIS = 60_000L;
    private static final AtomicLong LAST_WARNING = new AtomicLong();

    private SurrogateCharacterReferences() {
    }

    /** Repairs a complete document or fragment. Returns the argument itself if it holds no reference at all. */
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
     * writes the lookahead out and flushes the target without closing it.
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
     * the document's own encoding declaration. A UTF-16 or UTF-32 stream, recognised by a
     * byte-order mark or a NUL among its first four bytes, passes through unchanged: its
     * non-ASCII characters could otherwise spell a reference byte for byte.
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

        void accept(final char c) {
            if (plain() && c != '&' && c != '<') {
                out.append(c);
            } else {
                in.append(c);
                drain(false);
            }
        }

        void accept(final CharSequence chars, final int from, final int to) {
            int i = from;
            while (i < to) {
                if (plain()) {
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
            final int to = off + len;
            int i = off;
            while (i < to) {
                if (plain()) {
                    int run = i;
                    while (run < to && chars[run] != '&' && chars[run] != '<') {
                        run++;
                    }
                    if (run > i) {
                        out.append(chars, i, run - i);
                        i = run;
                        continue;
                    }
                }
                in.append(chars[i++]);
                drain(false);
            }
        }

        /** Decides whatever is still held back. The scanner is ready for another document afterwards. */
        void finish() {
            drain(true);
            flushPendingLone();
            mode = Mode.TEXT;
            if (unpaired > 0) {
                final long now = System.currentTimeMillis();
                final long last = LAST_WARNING.get();
                if (now - last >= WARN_INTERVAL_MILLIS && LAST_WARNING.compareAndSet(last, now)) {
                    LOG.warn("Replaced {} unpaired surrogate character reference(s) with U+FFFD; further replacements are logged at debug level for a minute", unpaired);
                } else {
                    LOG.debug("Replaced {} unpaired surrogate character reference(s) with U+FFFD", unpaired);
                }
                unpaired = 0;
            }
        }

        int outputLength() {
            return out.length();
        }

        /** Removes up to len decided units from the front of the output. */
        int take(final char[] buf, final int off, final int len) {
            final int n = Math.min(len, out.length());
            out.getChars(0, n, buf, off);
            out.delete(0, n);
            return n;
        }

        /** Copies decided units without removing them; {@link #clearOutput()} removes them all at once. */
        void copyOutput(final int from, final char[] buf, final int len) {
            out.getChars(from, from + len, buf, 0);
        }

        void clearOutput() {
            out.setLength(0);
        }

        /** Plain text with nothing held back: the next unit can only matter if it opens markup or a reference. */
        private boolean plain() {
            return mode == Mode.TEXT && in.length() == 0 && pendingHigh < 0;
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
            while (i < in.length() && i - digits < (hex ? 6 : 7) && digit(in.charAt(i), hex) >= 0) {
                i++;
            }
            if (i == digits || i >= in.length() || in.charAt(i) != ';') {
                return -1;
            }
            return i + 1;
        }

        private int referenceValue(final int end) {
            final boolean hex = in.charAt(2) == 'x' || in.charAt(2) == 'X';
            int value = 0;
            for (int i = hex ? 3 : 2; i < end - 1; i++) {
                value = value * (hex ? 16 : 10) + digit(in.charAt(i), hex);
            }
            return value;
        }

        /** ASCII digits only: the XML production allows nothing else, unlike Character.digit(). */
        private static int digit(final char c, final boolean hex) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            if (hex && c >= 'a' && c <= 'f') {
                return c - 'a' + 10;
            }
            if (hex && c >= 'A' && c <= 'F') {
                return c - 'A' + 10;
            }
            return -1;
        }

        private boolean startsWith(final String s) {
            if (in.length() < s.length()) {
                return false;
            }
            for (int i = 0; i < s.length(); i++) {
                if (in.charAt(i) != s.charAt(i)) {
                    return false;
                }
            }
            return true;
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

    /**
     * MOXy writes text one character at a time, so decided output is handed on in chunks
     * rather than per character, and on flush, finish and close.
     */
    public static final class RepairingWriter extends Writer {
        private final Writer out;
        private final Scanner scanner = new Scanner(false);
        private final char[] buffer = new char[CHUNK];

        RepairingWriter(final Writer out) {
            this.out = Objects.requireNonNull(out);
        }

        @Override
        public void write(final int c) throws IOException {
            scanner.accept((char) c);
            if (scanner.outputLength() >= CHUNK) {
                forward();
            }
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

        /**
         * Writes the lookahead out, closing any pending pair, and flushes the target without
         * closing it. MOXy flushes the writer before this can run, so a caller that relies on
         * that flush gets one here.
         */
        public void finish() throws IOException {
            scanner.finish();
            flush();
        }

        @Override
        public void close() throws IOException {
            finish();
            out.close();
        }

        /** One pass over the decided output, so a single large write stays linear. */
        private void forward() throws IOException {
            final int length = scanner.outputLength();
            for (int from = 0; from < length; from += CHUNK) {
                final int n = Math.min(CHUNK, length - from);
                scanner.copyOutput(from, buffer, n);
                out.write(buffer, 0, n);
            }
            scanner.clearOutput();
        }
    }

    private static final class RepairingReader extends Reader {
        private final Reader in;
        private final Scanner scanner = new Scanner(false);
        private final char[] buffer = new char[CHUNK];
        private boolean eof;

        RepairingReader(final Reader in) {
            this.in = Objects.requireNonNull(in);
        }

        @Override
        public int read(final char[] cbuf, final int off, final int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            while (scanner.outputLength() == 0) {
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
        private final byte[] buffer = new byte[CHUNK];
        private final char[] chars = new char[CHUNK];
        private final byte[] one = new byte[1];
        /** Set once the first bytes have been seen: the raw stream with those bytes put back, or null to repair. */
        private InputStream passthrough;
        private boolean sniffed;
        private boolean eof;

        RepairingInputStream(final InputStream in) {
            this.in = Objects.requireNonNull(in);
        }

        @Override
        public int read() throws IOException {
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
            while (scanner.outputLength() == 0) {
                if (eof) {
                    return -1;
                }
                final int n = in.read(buffer);
                if (n < 0) {
                    eof = true;
                    scanner.finish();
                } else {
                    feed(n);
                }
            }
            // the scanner only ever adds ASCII, so every output unit fits a byte again
            final int n = scanner.take(chars, 0, Math.min(len, chars.length));
            for (int i = 0; i < n; i++) {
                b[off + i] = (byte) chars[i];
            }
            return n;
        }

        /**
         * A UTF-16 or UTF-32 document starts with a byte-order mark or has a NUL among its
         * first four bytes. Its non-ASCII characters can spell {@code &#55357;} byte for byte
         * (U+2623 U+3535 ... in UTF-16BE does), so such a stream is not scanned at all.
         */
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
                passthrough = new SequenceInputStream(new ByteArrayInputStream(buffer, 0, n), in);
                return;
            }
            feed(n);
            if (n < 4) {
                eof = true;
                scanner.finish();
            }
        }

        private void feed(final int n) {
            for (int i = 0; i < n; i++) {
                chars[i] = (char) (buffer[i] & 0xFF);
            }
            scanner.accept(chars, 0, n);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}

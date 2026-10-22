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
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repairs surrogate pairs that were written as two character references, such as
 * {@code &#55357;&#56489;} for U+1F4A9. MOXy's Writer output produces them (NMS-19814),
 * and no XML parser accepts them. Adjacent references for a high and a low half become
 * the character they encode; an unpaired half becomes U+FFFD. References inside comments,
 * CDATA sections, processing instructions and the document type declaration are literal
 * text and are left alone.
 *
 * Only references of up to seven decimal or six hexadecimal ASCII digits are recognised,
 * which covers everything the marshaller ever wrote. The scanner works in place with at
 * most ten units of lookahead and runs in linear time on any input; text that needs no
 * repair is never copied, and a string without a surrogate reference is returned as is.
 */
public final class SurrogateCharacterReferences {
    private static final Logger LOG = LoggerFactory.getLogger(SurrogateCharacterReferences.class);

    /** Longest unit of markup the scanner has to see at once: {@code &#1114111;} or {@code <![CDATA[}. */
    private static final int LOOKAHEAD = 10;

    /** Size of the filters' transfer buffers. */
    private static final int CHUNK = 8192;

    /** How far into a byte stream the XML declaration is looked for. */
    private static final int HEADER = 1024;

    /** Unpaired halves are data loss, but one bad document replayed over IPC must not flood the log. */
    private static final long WARN_INTERVAL_MILLIS = 60_000L;
    private static final AtomicLong LAST_WARNING = new AtomicLong();

    private SurrogateCharacterReferences() {
    }

    /** Repairs a complete document or fragment. Returns the argument itself if it holds no surrogate reference. */
    public static String repair(final String xml) {
        if (xml == null || !containsSurrogateReference(xml)) {
            return xml;
        }
        final Scanner scanner = new Scanner(false);
        scanner.accept(xml, 0, xml.length());
        scanner.finish();
        // the reference may have been literal text, in a CDATA section for instance
        return scanner.repaired() ? scanner.takeAll() : xml;
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
     * the document's own encoding declaration. The stream is scanned only when its encoding
     * is UTF-8 or another one in which every ASCII byte stands for itself; a UTF-16, UTF-32,
     * ISO-2022 or other stateful or multi-byte document passes through unchanged, as its
     * non-ASCII characters could otherwise spell a reference byte for byte.
     */
    public static InputStream repairing(final InputStream in) {
        return new RepairingInputStream(in);
    }

    /** Whether the text holds a reference to a surrogate code unit anywhere, markup or not. */
    static boolean containsSurrogateReference(final String xml) {
        final char[] window = new char[LOOKAHEAD];
        final int length = xml.length();
        for (int i = xml.indexOf('&'); i >= 0; i = xml.indexOf('&', i + 1)) {
            final int n = Math.min(LOOKAHEAD, length - i);
            xml.getChars(i, i + n, window, 0);
            final int end = referenceEnd(window, 0, n);
            if (end > 0 && isSurrogate(referenceValue(window, 0, end))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSurrogate(final int value) {
        return value >= Character.MIN_SURROGATE && value <= Character.MAX_SURROGATE;
    }

    /** End (exclusive) of the numeric reference starting at {@code at}, which holds {@code '&'}, or -1. */
    private static int referenceEnd(final char[] s, final int at, final int limit) {
        if (limit - at < 4 || s[at + 1] != '#') {
            return -1;
        }
        int i = at + 2;
        final boolean hex = s[i] == 'x' || s[i] == 'X';
        if (hex) {
            i++;
        }
        final int digits = i;
        while (i < limit && i - digits < (hex ? 6 : 7) && digit(s[i], hex) >= 0) {
            i++;
        }
        if (i == digits || i >= limit || s[i] != ';') {
            return -1;
        }
        return i + 1;
    }

    private static int referenceValue(final char[] s, final int at, final int end) {
        final boolean hex = s[at + 2] == 'x' || s[at + 2] == 'X';
        int value = 0;
        for (int i = at + (hex ? 3 : 2); i < end - 1; i++) {
            value = value * (hex ? 16 : 10) + digit(s[i], hex);
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

    /**
     * Incremental scanner over chars, or over the bytes of an ASCII-compatible encoding
     * widened to chars: everything it inspects is ASCII, and other units pass through.
     *
     * The buffer holds, in order, output that has not been taken yet ({@code start} to
     * {@code w}), nothing, and input that is not decided yet ({@code r} to {@code len}).
     * A repair never makes the text longer, so decided input is written back in place, and
     * as long as nothing has been repaired the write position equals the read position and
     * the text is not touched at all.
     */
    static final class Scanner {
        private enum Mode { TEXT, COMMENT, CDATA, PI, DOCTYPE }

        /** Emit repaired code points as numeric references (byte streams) instead of characters. */
        private final boolean references;
        private char[] buf = new char[CHUNK + 2 * LOOKAHEAD];
        private int start;
        private int w;
        private int r;
        private int len;
        private Mode mode = Mode.TEXT;
        /** Mode to return to when a comment or processing instruction ends. */
        private Mode resume = Mode.TEXT;
        /** Nesting of {@code [} inside the document type declaration. */
        private int depth;
        /** Quote of the open literal inside the document type declaration, or 0. */
        private char quote;
        private int pendingHigh = -1;
        private int unpaired = 0;
        private boolean repaired;

        Scanner(final boolean references) {
            this.references = references;
        }

        void accept(final char c) {
            ensure(1);
            buf[len++] = c;
        }

        void accept(final char[] src, final int off, final int n) {
            ensure(n);
            System.arraycopy(src, off, buf, len, n);
            len += n;
        }

        void accept(final CharSequence s, final int from, final int to) {
            final int n = to - from;
            ensure(n);
            if (s instanceof String) {
                ((String) s).getChars(from, to, buf, len);
            } else {
                for (int i = 0; i < n; i++) {
                    buf[len + i] = s.charAt(from + i);
                }
            }
            len += n;
        }

        void accept(final byte[] src, final int off, final int n) {
            ensure(n);
            for (int i = 0; i < n; i++) {
                buf[len + i] = (char) (src[off + i] & 0xFF);
            }
            len += n;
        }

        /** Reads from the reader straight into the buffer; returns what the reader returned. */
        int fill(final Reader in) throws IOException {
            ensure(CHUNK);
            final int n = in.read(buf, len, buf.length - len);
            if (n > 0) {
                len += n;
            }
            return n;
        }

        /** Units of input and untaken output held. */
        int buffered() {
            return len - start;
        }

        /** Whether anything has been repaired so far. */
        boolean repaired() {
            return repaired;
        }

        /** Decided units ready to be taken. */
        int available() {
            return w - start;
        }

        int take(final char[] dst, final int off, final int n) {
            final int count = Math.min(n, w - start);
            System.arraycopy(buf, start, dst, off, count);
            start += count;
            return count;
        }

        /** The scanner only ever adds ASCII, so every output unit of a byte stream fits a byte again. */
        int take(final byte[] dst, final int off, final int n) {
            final int count = Math.min(n, w - start);
            for (int i = 0; i < count; i++) {
                dst[off + i] = (byte) buf[start + i];
            }
            start += count;
            return count;
        }

        void writeTo(final Writer out) throws IOException {
            if (w > start) {
                out.write(buf, start, w - start);
                start = w;
            }
        }

        String takeAll() {
            final String s = new String(buf, start, w - start);
            start = w;
            return s;
        }

        private void ensure(final int n) {
            if (len + n <= buf.length) {
                return;
            }
            if (start > 0) {
                System.arraycopy(buf, start, buf, 0, len - start);
                len -= start;
                w -= start;
                r -= start;
                start = 0;
            }
            if (len + n > buf.length) {
                final char[] grown = new char[Math.max(buf.length * 2, len + n)];
                System.arraycopy(buf, 0, grown, 0, len);
                buf = grown;
            }
        }

        /** Decides whatever is still held back. The scanner is ready for another document afterwards. */
        void finish() {
            process(true);
            flushPendingLone();
            mode = Mode.TEXT;
            resume = Mode.TEXT;
            depth = 0;
            quote = 0;
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

        /** Decides as much of the input as possible; without eof, up to ten units stay undecided. */
        void process(final boolean eof) {
            while (r < len) {
                final boolean progressed;
                switch (mode) {
                case COMMENT:
                    progressed = closing("-->", eof);
                    break;
                case CDATA:
                    progressed = closing("]]>", eof);
                    break;
                case PI:
                    progressed = closing("?>", eof);
                    break;
                case DOCTYPE:
                    progressed = doctype(eof);
                    break;
                default:
                    progressed = text(eof);
                    break;
                }
                if (!progressed) {
                    return;
                }
            }
        }

        private boolean text(final boolean eof) {
            final char c = buf[r];
            if (c != '&' && c != '<') {
                flushPendingLone();
                int k = r + 1;
                while (k < len && buf[k] != '&' && buf[k] != '<') {
                    k++;
                }
                copy(k - r);
                return true;
            }
            if (len - r < LOOKAHEAD && !eof) {
                return false;
            }
            if (c == '<') {
                flushPendingLone();
                // nearly every '<' opens an ordinary tag
                final char next = len - r > 1 ? buf[r + 1] : 0;
                if (next == '?') {
                    enter(Mode.PI, Mode.TEXT, 2);
                } else if (next != '!') {
                    copy(1);
                } else if (at("<!--")) {
                    enter(Mode.COMMENT, Mode.TEXT, 4);
                } else if (at("<![CDATA[")) {
                    enter(Mode.CDATA, Mode.TEXT, 9);
                } else if (at("<!DOCTYPE")) {
                    enter(Mode.DOCTYPE, Mode.TEXT, 9);
                } else {
                    copy(1);
                }
                return true;
            }
            final int end = referenceEnd(buf, r, len);
            if (end < 0) {
                flushPendingLone();
                copy(1);
                return true;
            }
            final int value = referenceValue(buf, r, end);
            if (value >= Character.MIN_HIGH_SURROGATE && value <= Character.MAX_HIGH_SURROGATE) {
                flushPendingLone();
                pendingHigh = value;
                r = end;
            } else if (value >= Character.MIN_LOW_SURROGATE && value <= Character.MAX_LOW_SURROGATE) {
                r = end;
                if (pendingHigh >= 0) {
                    emit(Character.toCodePoint((char) pendingHigh, (char) value));
                    pendingHigh = -1;
                } else {
                    emitLone();
                }
            } else {
                flushPendingLone();
                copy(end - r);
            }
            return true;
        }

        /** Literal until the closing marker; text up to a possible marker is passed on at once. */
        private boolean closing(final String marker, final boolean eof) {
            final char first = marker.charAt(0);
            int k = r;
            while (k < len && buf[k] != first) {
                k++;
            }
            if (k > r) {
                copy(k - r);
                return true;
            }
            if (len - r < marker.length() && !eof) {
                return false;
            }
            if (at(marker)) {
                mode = resume;
                copy(marker.length());
            } else {
                copy(1);
            }
            return true;
        }

        /**
         * The document type declaration ends at the first {@code >} outside its internal
         * subset and outside a quoted literal. Literals may hold markup-like text, so nothing
         * in here is treated as a reference or as the start of a section.
         */
        private boolean doctype(final boolean eof) {
            final char c = buf[r];
            if (quote != 0) {
                int k = r;
                while (k < len && buf[k] != quote) {
                    k++;
                }
                if (k > r) {
                    copy(k - r);
                } else {
                    quote = 0;
                    copy(1);
                }
                return true;
            }
            switch (c) {
            case '"':
            case '\'':
                quote = c;
                copy(1);
                return true;
            case '[':
                depth++;
                copy(1);
                return true;
            case ']':
                if (depth > 0) {
                    depth--;
                }
                copy(1);
                return true;
            case '>':
                if (depth == 0) {
                    mode = Mode.TEXT;
                }
                copy(1);
                return true;
            case '<':
                if (len - r < 4 && !eof) {
                    return false;
                }
                if (at("<!--")) {
                    enter(Mode.COMMENT, Mode.DOCTYPE, 4);
                } else if (at("<?")) {
                    enter(Mode.PI, Mode.DOCTYPE, 2);
                } else {
                    copy(1);
                }
                return true;
            default:
                int k = r + 1;
                while (k < len && !doctypeMarkup(buf[k])) {
                    k++;
                }
                copy(k - r);
                return true;
            }
        }

        private static boolean doctypeMarkup(final char c) {
            return c == '"' || c == '\'' || c == '[' || c == ']' || c == '>' || c == '<';
        }

        private void enter(final Mode next, final Mode back, final int opener) {
            mode = next;
            resume = back;
            copy(opener);
        }

        private boolean at(final String s) {
            if (len - r < s.length()) {
                return false;
            }
            for (int i = 0; i < s.length(); i++) {
                if (buf[r + i] != s.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        /** Passes n input units on as output. Nothing moves while nothing has been repaired. */
        private void copy(final int n) {
            if (w != r) {
                System.arraycopy(buf, r, buf, w, n);
            }
            w += n;
            r += n;
        }

        /**
         * Writes a repaired code point at the output position. The references it replaces
         * took at least eight units, and a reference for any code point takes at most ten,
         * two references at least sixteen, so the output never catches up with the input.
         */
        private void emit(final int codePoint) {
            repaired = true;
            if (!references) {
                w += Character.toChars(codePoint, buf, w);
                return;
            }
            buf[w++] = '&';
            buf[w++] = '#';
            int digits = 1;
            for (int v = codePoint; v >= 10; v /= 10) {
                digits++;
            }
            for (int i = digits - 1, v = codePoint; i >= 0; i--, v /= 10) {
                buf[w + i] = (char) ('0' + v % 10);
            }
            w += digits;
            buf[w++] = ';';
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
     * MOXy writes text one character at a time, so input is collected and decided output
     * handed on in chunks rather than per character, and on flush, finish and close.
     */
    public static final class RepairingWriter extends Writer {
        private final Writer out;
        private final Scanner scanner = new Scanner(false);

        RepairingWriter(final Writer out) {
            this.out = Objects.requireNonNull(out);
        }

        @Override
        public void write(final int c) throws IOException {
            scanner.accept((char) c);
            if (scanner.buffered() >= CHUNK) {
                forward();
            }
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            for (int i = 0; i < len; i += CHUNK) {
                scanner.accept(cbuf, off + i, Math.min(CHUNK, len - i));
                forward();
            }
        }

        @Override
        public void write(final String str, final int off, final int len) throws IOException {
            for (int i = 0; i < len; i += CHUNK) {
                scanner.accept(str, off + i, off + i + Math.min(CHUNK, len - i));
                forward();
            }
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

        private void forward() throws IOException {
            scanner.process(false);
            scanner.writeTo(out);
        }
    }

    private static final class RepairingReader extends Reader {
        private final Reader in;
        private final Scanner scanner = new Scanner(false);
        private boolean eof;

        RepairingReader(final Reader in) {
            this.in = Objects.requireNonNull(in);
        }

        @Override
        public int read(final char[] cbuf, final int off, final int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            while (scanner.available() == 0) {
                if (eof) {
                    return -1;
                }
                if (scanner.fill(in) < 0) {
                    eof = true;
                    scanner.finish();
                } else {
                    scanner.process(false);
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
            while (scanner.available() == 0) {
                if (eof) {
                    return -1;
                }
                final int n = in.read(buffer);
                if (n < 0) {
                    eof = true;
                    scanner.finish();
                } else {
                    scanner.accept(buffer, 0, n);
                    scanner.process(false);
                }
            }
            return scanner.take(b, off, len);
        }

        /**
         * Reads the start of the document, far enough to see an XML declaration, and decides
         * whether the stream can be scanned at all. A byte-order mark or a NUL among the first
         * four bytes means UTF-16 or UTF-32; a first byte that is not ASCII markup or white
         * space means an encoding such as EBCDIC; otherwise the declared encoding decides.
         */
        private void sniff() throws IOException {
            sniffed = true;
            int n = 0;
            boolean end = false;
            while (n < HEADER && !settled(buffer, n)) {
                final int r = in.read(buffer, n, HEADER - n);
                if (r < 0) {
                    end = true;
                    break;
                }
                n += r;
            }
            if (!scannable(buffer, n)) {
                passthrough = new SequenceInputStream(new ByteArrayInputStream(buffer, 0, n), in);
                return;
            }
            scanner.accept(buffer, 0, n);
            if (end) {
                eof = true;
                scanner.finish();
            } else {
                scanner.process(false);
            }
        }

        /** Enough has been read once the start is known not to be a declaration, or the declaration is complete. */
        private static boolean settled(final byte[] b, final int n) {
            return n >= 8 && (!startsWithDeclaration(b, n) || indexOf(b, n, "?>") >= 0);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private static boolean scannable(final byte[] b, final int n) {
        boolean wide = n >= 2 && ((b[0] == (byte) 0xFE && b[1] == (byte) 0xFF) || (b[0] == (byte) 0xFF && b[1] == (byte) 0xFE));
        for (int i = 0; i < Math.min(n, 4); i++) {
            wide |= b[i] == 0;
        }
        if (wide) {
            return false;
        }
        final int first = bomLength(b, n);
        if (first < n) {
            final int c = b[first] & 0xFF;
            if (c != '<' && c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return false;
            }
        }
        return asciiCompatible(declaredEncoding(b, n));
    }

    private static int bomLength(final byte[] b, final int n) {
        return n >= 3 && b[0] == (byte) 0xEF && b[1] == (byte) 0xBB && b[2] == (byte) 0xBF ? 3 : 0;
    }

    private static boolean startsWithDeclaration(final byte[] b, final int n) {
        final int at = bomLength(b, n);
        return n - at >= 5 && b[at] == '<' && b[at + 1] == '?' && b[at + 2] == 'x' && b[at + 3] == 'm' && b[at + 4] == 'l';
    }

    private static int indexOf(final byte[] b, final int n, final String s) {
        outer:
        for (int i = 0; i + s.length() <= n; i++) {
            for (int j = 0; j < s.length(); j++) {
                if (b[i + j] != s.charAt(j)) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /** The value of the declaration's encoding pseudo-attribute, or null when there is none. */
    static String declaredEncoding(final byte[] b, final int n) {
        if (!startsWithDeclaration(b, n)) {
            return null;
        }
        final int end = indexOf(b, n, "?>");
        final int limit = end < 0 ? n : end;
        int i = indexOf(b, limit, "encoding");
        if (i < 0) {
            return null;
        }
        i += "encoding".length();
        while (i < limit && (b[i] == ' ' || b[i] == '\t' || b[i] == '\n' || b[i] == '\r')) {
            i++;
        }
        if (i >= limit || b[i] != '=') {
            return null;
        }
        i++;
        while (i < limit && (b[i] == ' ' || b[i] == '\t' || b[i] == '\n' || b[i] == '\r')) {
            i++;
        }
        if (i >= limit || (b[i] != '"' && b[i] != '\'')) {
            return null;
        }
        final byte quote = b[i++];
        final StringBuilder name = new StringBuilder();
        while (i < limit && b[i] != quote) {
            name.append((char) (b[i++] & 0xFF));
        }
        return i < limit ? name.toString() : null;
    }

    /**
     * Whether every ASCII byte of the encoding stands for the ASCII character, so that the
     * scanner can read markup and references off the bytes. UTF-8 and the single-byte
     * supersets of ASCII qualify; an unknown encoding is left to the parser untouched.
     */
    static boolean asciiCompatible(final String encoding) {
        if (encoding == null) {
            return true;
        }
        final String name;
        try {
            name = Charset.forName(encoding).name().toUpperCase(Locale.ROOT);
        } catch (final RuntimeException e) {
            return false;
        }
        return name.equals("UTF-8") || name.equals("US-ASCII") || name.startsWith("ISO-8859-")
                || name.startsWith("WINDOWS-125") || name.startsWith("KOI8-") || name.equals("TIS-620");
    }
}

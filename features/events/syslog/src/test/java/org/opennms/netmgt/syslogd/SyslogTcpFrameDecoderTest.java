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
package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.syslogd.SyslogTcpFraming;
import org.opennms.netmgt.syslogd.api.SyslogConnection;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

public class SyslogTcpFrameDecoderTest {

    private static final InetSocketAddress SOURCE = new InetSocketAddress("192.168.1.1", 43210);

    private static final String MESSAGE = "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8";
    private static final String OTHER_MESSAGE = "<13>Oct 11 22:14:16 otherhost sshd: accepted publickey for lonvick";

    private static final int DEFAULT_MAX = 65536;

    // --- non-transparent (LF) framing ---------------------------------------

    @Test
    public void decodesSingleLfDelimitedMessage() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        assertTrue(channel.writeInbound(buf(MESSAGE + "\n")));

        assertEquals(List.of(MESSAGE), drain(channel));
        assertEquals(SyslogTcpFraming.NON_TRANSPARENT, decoderOf(channel).getActiveFraming());
    }

    @Test
    public void decodesMultipleLfDelimitedMessagesInOneBuffer() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\n" + OTHER_MESSAGE + "\n"));

        assertEquals(List.of(MESSAGE, OTHER_MESSAGE), drain(channel));
    }

    @Test
    public void decodesLfDelimitedMessageSplitAtEveryBoundary() {
        final String wire = MESSAGE + "\n";
        for (int split = 1; split < wire.length(); split++) {
            final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

            channel.writeInbound(buf(wire.substring(0, split)));
            channel.writeInbound(buf(wire.substring(split)));

            assertEquals("split at " + split, List.of(MESSAGE), drain(channel));
        }
    }

    @Test
    public void stripsCrFromCrlfLineEndings() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\r\n" + OTHER_MESSAGE + "\r\n"));

        // Nothing downstream strips a CR, so a leaked one would end up in the event body.
        assertEquals(List.of(MESSAGE, OTHER_MESSAGE), drain(channel));
    }

    @Test
    public void stripsTrailingNul() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\0\n"));

        assertEquals(List.of(MESSAGE), drain(channel));
    }

    @Test
    public void bareLfKeepaliveProducesNothingAndNoError() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf("\n\n\n"));

        assertEquals(List.of(), drain(channel));
        assertTrue(channel.isActive());
        // No frame has been seen, so auto-detection must not have latched yet.
        assertNull(decoderOf(channel).getActiveFraming());

        channel.writeInbound(buf(MESSAGE + "\n"));
        assertEquals(List.of(MESSAGE), drain(channel));
    }

    @Test
    public void keepalivesBetweenMessagesAreIgnored() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\n\n\n" + OTHER_MESSAGE + "\n"));

        assertEquals(List.of(MESSAGE, OTHER_MESSAGE), drain(channel));
    }

    // --- octet-counting framing --------------------------------------------

    @Test
    public void decodesSingleOctetCountedMessage() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(octetCounted(MESSAGE)));

        assertEquals(List.of(MESSAGE), drain(channel));
        assertEquals(SyslogTcpFraming.OCTET_COUNTING, decoderOf(channel).getActiveFraming());
    }

    @Test
    public void decodesMultipleOctetCountedMessagesInOneBuffer() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(octetCounted(MESSAGE) + octetCounted(OTHER_MESSAGE)));

        assertEquals(List.of(MESSAGE, OTHER_MESSAGE), drain(channel));
    }

    @Test
    public void decodesOctetCountedMessageSplitAtEveryBoundary() {
        // Includes splits inside the length prefix, which is where a decoder that parses
        // the prefix before checking for completeness goes wrong.
        final String wire = octetCounted(MESSAGE);
        for (int split = 1; split < wire.length(); split++) {
            final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

            channel.writeInbound(buf(wire.substring(0, split)));
            channel.writeInbound(buf(wire.substring(split)));

            assertEquals("split at " + split, List.of(MESSAGE), drain(channel));
        }
    }

    @Test
    public void octetCountedMessageIsNotSplitOnEmbeddedNewlines() {
        final String multiline = "<34>Oct 11 22:14:15 mymachine app: line one\nline two\nline three";
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(octetCounted(multiline)));

        // Preserving embedded newlines is the whole point of octet counting.
        assertEquals(List.of(multiline), drain(channel));
    }

    @Test
    public void zeroLengthOctetCountedFrameIsSkipped() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf("0 " + octetCounted(MESSAGE)));

        assertEquals(List.of(MESSAGE), drain(channel));
        assertTrue(channel.isActive());
    }

    // --- limits and malformed input ----------------------------------------

    @Test
    public void octetCountedFrameOverMaximumIsRejected() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, 128);

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(buf("4096 " + MESSAGE)));
    }

    @Test
    public void lfDelimitedFrameOverMaximumIsDiscardedRatherThanFatal() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, 32);

        channel.writeInbound(buf(MESSAGE + "\n"));

        assertEquals(List.of(), drain(channel));
    }

    @Test
    public void unterminatedLfDelimitedFrameOverMaximumIsRejected() {
        // A sender that never sends a trailer must not be able to grow the cumulation
        // buffer without bound.
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, 32);

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(buf(MESSAGE)));
    }

    @Test
    public void malformedLengthPrefixIsRejected() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.OCTET_COUNTING, DEFAULT_MAX);

        assertThrows(CorruptedFrameException.class, () -> channel.writeInbound(buf("12x4 " + MESSAGE)));
    }

    @Test
    public void lengthPrefixWithoutSeparatorIsRejected() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.OCTET_COUNTING, DEFAULT_MAX);

        assertThrows(CorruptedFrameException.class, () -> channel.writeInbound(buf("123456789012345")));
    }

    // --- pipeline behaviour on a framing error ------------------------------

    @Test
    public void oversizeOctetCountedFrameClosesTheConnection() {
        final EmbeddedChannel channel = guardedChannel(SyslogTcpFraming.AUTO, 32);

        // A length prefix past the maximum leaves the next message nowhere in particular.
        channel.writeInbound(buf(octetCounted(MESSAGE)));

        assertFalse(channel.isActive());
        assertEquals(List.of(), drain(channel));
    }

    @Test
    public void malformedFrameClosesTheConnection() {
        final EmbeddedChannel channel = guardedChannel(SyslogTcpFraming.OCTET_COUNTING, DEFAULT_MAX);

        channel.writeInbound(buf("12x4 " + MESSAGE));

        // The stream cannot be resynchronised after a framing error, so the connection
        // is dropped rather than carrying on with an unknown message boundary.
        assertFalse(channel.isActive());
        assertEquals(List.of(), drain(channel));
    }

    @Test
    public void messagesBeforeAFramingErrorAreStillDelivered() {
        final EmbeddedChannel channel = guardedChannel(SyslogTcpFraming.OCTET_COUNTING, DEFAULT_MAX);

        channel.writeInbound(buf(octetCounted(MESSAGE) + "12x4 " + OTHER_MESSAGE));

        assertEquals(List.of(MESSAGE), drain(channel));
        assertFalse(channel.isActive());
    }

    // --- framing selection --------------------------------------------------

    @Test
    public void detectedFramingIsLatchedForTheLifeOfTheConnection() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(octetCounted(MESSAGE)));
        assertEquals(List.of(MESSAGE), drain(channel));
        assertEquals(SyslogTcpFraming.OCTET_COUNTING, decoderOf(channel).getActiveFraming());

        // A sender that switches framing mid-stream cannot be decoded unambiguously, so
        // this must fail loudly rather than silently reinterpreting the stream.
        assertThrows(CorruptedFrameException.class, () -> channel.writeInbound(buf(MESSAGE + "\n")));
    }

    @Test
    public void forcedOctetCountingIgnoresLfDelimitedInput() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.OCTET_COUNTING, DEFAULT_MAX);

        assertEquals(SyslogTcpFraming.OCTET_COUNTING, decoderOf(channel).getActiveFraming());
        assertThrows(CorruptedFrameException.class, () -> channel.writeInbound(buf(MESSAGE + "\n")));
    }

    @Test
    public void forcedNonTransparentTreatsLengthPrefixAsMessageContent() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.NON_TRANSPARENT, DEFAULT_MAX);

        assertEquals(SyslogTcpFraming.NON_TRANSPARENT, decoderOf(channel).getActiveFraming());
        channel.writeInbound(buf(octetCounted(MESSAGE) + "\n"));

        // The forced mode wins over what the bytes look like. This is the framing
        // mismatch operators see as a leaked length prefix in the event body.
        assertEquals(List.of(MESSAGE.length() + " " + MESSAGE), drain(channel));
    }

    // --- payload handoff ----------------------------------------------------

    @Test
    public void emittedBufferIsExactlyTheMessageLength() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\n"));

        final SyslogConnection connection = channel.readInbound();
        final ByteBuffer buffer = connection.getBuffer();
        // ByteBufferXmlAdapter marshals the whole backing array, so slack in the copy
        // would reach the consumer as trailing garbage.
        assertEquals(MESSAGE.length(), buffer.array().length);
        assertEquals(MESSAGE.length(), buffer.limit());
        assertEquals(0, buffer.position());
    }

    @Test
    public void sourceAddressIsCarriedOnEveryMessage() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\n" + OTHER_MESSAGE + "\n"));

        SyslogConnection connection;
        int seen = 0;
        while ((connection = channel.readInbound()) != null) {
            assertEquals(SOURCE, connection.getSource());
            seen++;
        }
        assertEquals(2, seen);
    }

    // --- helpers ------------------------------------------------------------

    /** Decoder alone, so that a framing error surfaces as the thrown exception. */
    @Test
    public void splitsNulDelimitedMessages() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.AUTO, DEFAULT_MAX);

        // Some appliances trail with NUL instead of LF. Nothing arrived at all before this.
        channel.writeInbound(buf(MESSAGE + "\0" + OTHER_MESSAGE + "\0"));

        assertEquals(List.of(MESSAGE, OTHER_MESSAGE), drain(channel));
        assertEquals(SyslogTcpFraming.NON_TRANSPARENT, decoderOf(channel).getActiveFraming());
    }

    @Test
    public void splitsAMixOfNulAndNewlineDelimiters() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.NON_TRANSPARENT, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE + "\0" + OTHER_MESSAGE + "\n" + MESSAGE + "\r\n"));

        assertEquals(List.of(MESSAGE, OTHER_MESSAGE, MESSAGE), drain(channel));
    }

    @Test
    public void treatsABareNulAsAKeepalive() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.NON_TRANSPARENT, DEFAULT_MAX);

        channel.writeInbound(buf("\0\0" + MESSAGE + "\0"));

        assertEquals(List.of(MESSAGE), drain(channel));
    }

    @Test
    public void waitsForATrailerAcrossWrites() {
        final EmbeddedChannel channel = channel(SyslogTcpFraming.NON_TRANSPARENT, DEFAULT_MAX);

        channel.writeInbound(buf(MESSAGE.substring(0, 20)));
        assertEquals(List.of(), drain(channel));

        channel.writeInbound(buf(MESSAGE.substring(20) + "\0"));
        assertEquals(List.of(MESSAGE), drain(channel));
    }

    @Test
    public void skipsAnOversizedLineAndKeepsTheConnection() {
        final EmbeddedChannel channel = guardedChannel(SyslogTcpFraming.NON_TRANSPARENT, 64);
        final String tooLong = "<34>" + "x".repeat(200);
        final String short_ = "<34>Oct 11 22:14:15 h a: ok";

        channel.writeInbound(buf(tooLong + "\n" + short_ + "\n"));

        // The trailer is known, so only the offending message is lost.
        assertEquals(List.of(short_), drain(channel));
        assertTrue("the connection must survive one overlong line", channel.isOpen());
    }

    @Test
    public void stillClosesWhenNoTrailerArrivesWithinTheLimit() {
        final EmbeddedChannel channel = guardedChannel(SyslogTcpFraming.NON_TRANSPARENT, 64);

        // No delimiter anywhere, so the position of the next message is unknown.
        channel.writeInbound(buf("<34>" + "x".repeat(200)));

        assertEquals(List.of(), drain(channel));
        assertFalse(channel.isOpen());
    }

    private static EmbeddedChannel channel(final SyslogTcpFraming framing, final int maxMessageSize) {
        return new EmbeddedChannel(new SyslogTcpFrameDecoder(SOURCE, framing, maxMessageSize));
    }

    /** Decoder plus the tail handler the listener installs, so errors close the channel. */
    private static EmbeddedChannel guardedChannel(final SyslogTcpFraming framing, final int maxMessageSize) {
        return new EmbeddedChannel(new SyslogTcpFrameDecoder(SOURCE, framing, maxMessageSize),
                new SyslogTcpExceptionHandler(SOURCE));
    }

    private static SyslogTcpFrameDecoder decoderOf(final EmbeddedChannel channel) {
        return channel.pipeline().get(SyslogTcpFrameDecoder.class);
    }

    private static io.netty.buffer.ByteBuf buf(final String content) {
        return Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
    }

    private static String octetCounted(final String message) {
        return message.getBytes(StandardCharsets.UTF_8).length + " " + message;
    }

    private static List<String> drain(final EmbeddedChannel channel) {
        final List<String> messages = new ArrayList<>();
        SyslogConnection connection;
        while ((connection = channel.readInbound()) != null) {
            final ByteBuffer buffer = connection.getBuffer();
            final byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            messages.add(new String(bytes, StandardCharsets.UTF_8));
        }
        return messages;
    }
}

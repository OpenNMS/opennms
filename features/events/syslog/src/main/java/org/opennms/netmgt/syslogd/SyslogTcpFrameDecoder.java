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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.config.syslogd.SyslogTcpFraming;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.ByteProcessor;

/**
 * Splits a syslog TCP stream into individual messages, emitting one
 * {@link SyslogConnection} per message.
 *
 * RFC 6587 defines two incompatible framings and senders disagree about which to use by
 * default, so both are supported. Under {@link SyslogTcpFraming#AUTO} the framing is
 * detected from the first frame and then latched: guessing per frame would turn a sender
 * that switches framing mid-stream into corrupt events rather than a visible error.
 *
 * One instance per channel, not thread safe.
 */
public class SyslogTcpFrameDecoder extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogTcpFrameDecoder.class);

    /** Bounds the prefix scan, so a sender that never emits the space cannot grow the buffer. */
    private static final int MAX_LENGTH_PREFIX_DIGITS = 10;

    private static final byte SPACE = ' ';
    private static final byte LF = '\n';
    private static final byte CR = '\r';
    private static final byte NUL = 0;

    /** Either byte ends a frame: LF is the usual trailer, NUL is what some appliances send. */
    private static final ByteProcessor FIND_TRAILER = value -> value != LF && value != NUL;

    private final InetSocketAddress source;
    private final SyslogTcpFraming configuredFraming;
    private final int maxMessageSize;

    private SyslogTcpFraming activeFraming;

    public SyslogTcpFrameDecoder(final InetSocketAddress source, final SyslogTcpFraming configuredFraming, final int maxMessageSize) {
        this.source = Objects.requireNonNull(source);
        this.configuredFraming = Objects.requireNonNull(configuredFraming);
        if (maxMessageSize < 1) {
            throw new IllegalArgumentException("maxMessageSize must be positive");
        }
        this.maxMessageSize = maxMessageSize;
        this.activeFraming = configuredFraming == SyslogTcpFraming.AUTO ? null : configuredFraming;
    }

    /** The framing for this connection, or null until auto-detection has seen a frame. */
    public SyslogTcpFraming getActiveFraming() {
        return activeFraming;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (activeFraming == null) {
            // Empty frames carry no signal: a stream of keepalives would otherwise latch
            // non-transparent framing before the first real message.
            skipEmptyFrameTrailers(in);
            if (!in.isReadable()) {
                return;
            }
            final byte first = in.getByte(in.readerIndex());
            activeFraming = (first >= '0' && first <= '9')
                    ? SyslogTcpFraming.OCTET_COUNTING
                    : SyslogTcpFraming.NON_TRANSPARENT;
            LOG.info("Detected {} framing for syslog TCP connection from {}", activeFraming, source);
        }

        if (activeFraming == SyslogTcpFraming.OCTET_COUNTING) {
            decodeOctetCounting(in, out);
        } else {
            decodeNonTransparent(in, out);
        }
    }

    /**
     * RFC 6587 section 3.4.1. MSG-LEN counts only SYSLOG-MSG, so the frame is complete
     * once the prefix, the space and that many further octets have arrived.
     */
    private void decodeOctetCounting(final ByteBuf in, final List<Object> out) {
        while (true) {
            final int start = in.readerIndex();
            final int scanLimit = Math.min(in.writerIndex(), start + MAX_LENGTH_PREFIX_DIGITS + 1);

            int spaceIndex = -1;
            for (int i = start; i < scanLimit; i++) {
                final byte b = in.getByte(i);
                if (b == SPACE) {
                    spaceIndex = i;
                    break;
                }
                if (b < '0' || b > '9') {
                    throw new CorruptedFrameException(String.format(
                            "Malformed octet-counted syslog frame from %s: unexpected byte 0x%02X in the length prefix",
                            source, b));
                }
            }

            if (spaceIndex < 0) {
                if (scanLimit - start > MAX_LENGTH_PREFIX_DIGITS) {
                    throw new CorruptedFrameException(String.format(
                            "Malformed octet-counted syslog frame from %s: no separator within %d bytes of the length prefix",
                            source, MAX_LENGTH_PREFIX_DIGITS));
                }
                return;
            }

            if (spaceIndex == start) {
                throw new CorruptedFrameException(String.format(
                        "Malformed octet-counted syslog frame from %s: empty length prefix", source));
            }

            // A long, so a 10 digit prefix cannot overflow before the maximum check.
            long messageLength = 0;
            for (int i = start; i < spaceIndex; i++) {
                messageLength = messageLength * 10 + (in.getByte(i) - '0');
            }

            if (messageLength > maxMessageSize) {
                throw new TooLongFrameException(String.format(
                        "Octet-counted syslog frame from %s declares %d bytes, which exceeds the %d byte maximum",
                        source, messageLength, maxMessageSize));
            }

            final int headerLength = spaceIndex + 1 - start;
            if (messageLength == 0) {
                // Not legal per the grammar but harmless; consume it so the loop progresses.
                in.skipBytes(headerLength);
                LOG.debug("Discarding zero length octet-counted syslog frame from {}", source);
                continue;
            }

            if (in.readableBytes() < headerLength + messageLength) {
                return;
            }

            in.skipBytes(headerLength);
            out.add(toConnection(in, in.readerIndex(), (int) messageLength));
            in.skipBytes((int) messageLength);
        }
    }

    /**
     * RFC 6587 section 3.4.2. LF or NUL separates messages, and a trailing CR is stripped
     * because senders append it and nothing downstream would.
     */
    private void decodeNonTransparent(final ByteBuf in, final List<Object> out) {
        while (true) {
            skipEmptyFrameTrailers(in);
            if (!in.isReadable()) {
                return;
            }

            final int start = in.readerIndex();
            final int trailerIndex = in.forEachByte(start, in.readableBytes(), FIND_TRAILER);

            if (trailerIndex < 0) {
                if (in.readableBytes() > maxMessageSize) {
                    throw new TooLongFrameException(String.format(
                            "Syslog frame from %s exceeds the %d byte maximum with no trailer in sight",
                            source, maxMessageSize));
                }
                return;
            }

            final int frameLength = trailerIndex - start;
            if (frameLength > maxMessageSize) {
                // Drop the message rather than the connection: the trailer is already in
                // hand, so the next message starts right after it. Only the octet-counted
                // path has to close, because there a bad length leaves the next message
                // nowhere in particular.
                LOG.warn("Discarding a {} byte syslog frame from {}, which exceeds the {} byte maximum",
                        frameLength, source, maxMessageSize);
                in.skipBytes(frameLength + 1);
                continue;
            }

            final int trimmedLength = trimTrailingTrailers(in, start, frameLength);
            if (trimmedLength > 0) {
                out.add(toConnection(in, start, trimmedLength));
            }
            // Consume the frame and its trailer whether or not it produced a message.
            in.skipBytes(frameLength + 1);
        }
    }

    /** Skips trailers with nothing in front of them: rsyslog sends a bare LF as a keepalive. */
    private void skipEmptyFrameTrailers(final ByteBuf in) {
        while (in.isReadable()) {
            final byte b = in.getByte(in.readerIndex());
            if (b != LF && b != CR && b != NUL) {
                return;
            }
            in.skipBytes(1);
        }
    }

    private int trimTrailingTrailers(final ByteBuf in, final int start, final int length) {
        int trimmed = length;
        while (trimmed > 0) {
            final byte b = in.getByte(start + trimmed - 1);
            if (b != CR && b != NUL) {
                break;
            }
            trimmed--;
        }
        return trimmed;
    }

    /**
     * Copies the frame out, since the dispatch is asynchronous and Netty recycles the buffer
     * as soon as decoding returns. Sized exactly: ByteBufferXmlAdapter marshals the whole
     * backing array, so slack would reach the consumer as trailing garbage.
     */
    private SyslogConnection toConnection(final ByteBuf in, final int index, final int length) {
        final ByteBuffer copy = ByteBuffer.allocate(length);
        in.getBytes(index, copy);
        copy.flip();
        return new SyslogConnection(source, copy);
    }
}

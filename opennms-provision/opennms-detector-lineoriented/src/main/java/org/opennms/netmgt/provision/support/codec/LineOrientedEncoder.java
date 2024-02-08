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
package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LineOrientedEncoder class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class LineOrientedEncoder extends ProtocolEncoderAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(LineOrientedEncoder.class);
    private final AttributeKey ENCODER = new AttributeKey(getClass(), "encoder");
    private final Charset m_charset;
    
    /**
     * <p>Constructor for LineOrientedEncoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public LineOrientedEncoder(final Charset charset) {
        m_charset = charset;
    }
    
    /** {@inheritDoc} */
    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        final LineOrientedRequest request = (LineOrientedRequest) message;

        if(request.getRequest().contains("null")) {

            return;
        }

        CharsetEncoder encoder = (CharsetEncoder) session.getAttribute(ENCODER);
        if (encoder == null) {
            encoder = m_charset.newEncoder();
            session.setAttribute(ENCODER, encoder);
        }

        final String value = request.getRequest();
        IoBuffer buffer = null;
        try {
            buffer = IoBuffer.allocate(value.length()).setAutoExpand(true);
            buffer.putString(request.getRequest(), encoder);

            buffer.flip();
            LOG.debug("Client sending: {}", value.trim());
            out.write(buffer);
        } finally {
            if (buffer != null) {
                buffer.free();
            }
        }
    }

}

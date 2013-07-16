/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

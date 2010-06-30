/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;

/**
 * <p>LineOrientedEncoder class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public class LineOrientedEncoder extends ProtocolEncoderAdapter {
    private final AttributeKey ENCODER = new AttributeKey(getClass(), "encoder");
    private final Charset m_charset;
    
    /**
     * <p>Constructor for LineOrientedEncoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public LineOrientedEncoder(Charset charset) {
        m_charset = charset;
    }
    
    /** {@inheritDoc} */
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        LineOrientedRequest request = (LineOrientedRequest) message;
        
        if(request.getRequest().contains("null")) {

            return;
        }
        
        CharsetEncoder encoder = (CharsetEncoder) session.getAttribute(ENCODER);
        if (encoder == null) {
            encoder = m_charset.newEncoder();
            session.setAttribute(ENCODER, encoder);
        }
        
        String value = request.getRequest();
        IoBuffer buffer = IoBuffer.allocate(value.length()).setAutoExpand(true);
        
        buffer.putString(request.getRequest(), encoder);
        
        buffer.flip();
        LogUtils.infof(this, "Client sending: %s\n", request.getRequest().trim());
        out.write(buffer);

    }

}

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class HttpProtocolCodecFactory implements ProtocolCodecFactory {
    
    private LineOrientedEncoder m_encoder;
    private HttpStatusResponseDecoder m_decoder;
    
    public HttpProtocolCodecFactory() {
        this(Charset.defaultCharset());
    }
    
    public HttpProtocolCodecFactory(Charset charset) {
        m_encoder = new LineOrientedEncoder(charset);
        m_decoder = new HttpStatusResponseDecoder(charset);
    }
    
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return m_decoder;
    }

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return m_encoder;
    }

}

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * <p>MultilineHttpProtocolFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MultilineHttpProtocolFactory implements ProtocolCodecFactory {
    
    private LineOrientedEncoder m_encoder;
    private MultilineHttpStatusResponseDecoder m_decoder;
    
    /**
     * <p>Constructor for MultilineHttpProtocolFactory.</p>
     */
    public MultilineHttpProtocolFactory() {
        this(Charset.defaultCharset());
    }
    
    /**
     * <p>Constructor for MultilineHttpProtocolFactory.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public MultilineHttpProtocolFactory(Charset charset) {
        m_encoder = new LineOrientedEncoder(charset);
        m_decoder = new MultilineHttpStatusResponseDecoder(charset);
    }
    
    /** {@inheritDoc} */
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return m_decoder;
    }

    /** {@inheritDoc} */
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return m_encoder;
    }

}

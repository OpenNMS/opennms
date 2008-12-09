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
import java.util.StringTokenizer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.opennms.netmgt.provision.detector.LineOrientedResponse;

/**
 * @author Donald Desloge
 *
 */
public class LineOrientedDecoder extends CumulativeProtocolDecoder {
    
    private Charset m_charset;
    private String m_delimit;
    
    public LineOrientedDecoder(Charset charset, String delimit) {
        m_charset = charset;
        if(delimit == null) {
            m_delimit = "\r\n";
        }else {
            m_delimit = delimit;
        }
    }
    
    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        
        if(in.hasRemaining()) {
           String response = in.getString(m_charset.newDecoder());
           StringTokenizer tokenizer = new StringTokenizer(response, m_delimit);
           
           while(tokenizer.hasMoreTokens()) {
              response = tokenizer.nextToken();
              LineOrientedResponse lineResponse = new LineOrientedResponse(response);
              out.write(lineResponse);
           }
           
           return true;
        }else {
            return false;
        }
        
    }

    
}

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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;

/**
 * <p>MultiLineDecoder class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class MultiLineDecoder extends CumulativeProtocolDecoder {
    
    private final String m_multilineIndicator;
    private Charset m_charset;
    protected String CURRENT_RESPONSE = "CURRENT_RESPONSE";
    
    /**
     * <p>Constructor for MultiLineDecoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     * @param multilineIndicator a {@link java.lang.String} object.
     */
    public MultiLineDecoder(Charset charset, String multilineIndicator) {
        setCharset(charset);
        m_multilineIndicator = multilineIndicator;
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        MultilineOrientedResponse response = (MultilineOrientedResponse) session.getAttribute(CURRENT_RESPONSE);
        if(response == null) {
            response = new MultilineOrientedResponse();
            session.setAttribute(CURRENT_RESPONSE, response);
        }
        // Remember the initial position.
        int start = in.position();
        
        // Now find the first CRLF in the buffer.
        byte previous = 0;
        while (in.hasRemaining()) {
            byte current = in.get();
            
            if (previous == '\r' && current == '\n') {
                // Remember the current position and limit.
                int position = in.position();
                int limit = in.limit();
                try {
                    in.position(start);
                    in.limit(position);
                    // The bytes between in.position() and in.limit()
                    // now contain a full CRLF terminated line.
                    
                   if(!checkIndicator(in.slice())) { 
                       response.addLine(in.getString(getCharset().newDecoder()));
                       out.write(response);
                       session.removeAttribute(CURRENT_RESPONSE);
                       return true;
                    }else {
                       response.addLine(in.getString(getCharset().newDecoder()));
                    }
                    
                    
                } finally {
                    // Set the position to point right after the
                    // detected line and set the limit to the old
                    // one.
                    in.position(position);
                    in.limit(limit);
                }
                // Decoded one line; CumulativeProtocolDecoder will
                // call me again until I return false. So just
                // return true until there are no more lines in the
                // buffer.
                return true;
                
                }
            
            previous = current;
        }
        // Could not find CRLF in the buffer. Reset the initial
        // position to the one we recorded above.
        in.position(start);
        
        return false;
    }
    
    /**
     * <p>checkIndicator</p>
     *
     * @param in a {@link org.apache.mina.core.buffer.IoBuffer} object.
     * @return a boolean.
     * @throws java.nio.charset.CharacterCodingException if any.
     */
    protected boolean checkIndicator(IoBuffer in) throws CharacterCodingException {
        String line = in.getString(getCharset().newDecoder());
        return line.substring(3, 4).equals(getMultilineIndicator());
    }

    /**
     * <p>setCharset</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public void setCharset(Charset charset) {
        m_charset = charset;
    }

    /**
     * <p>getCharset</p>
     *
     * @return a {@link java.nio.charset.Charset} object.
     */
    public Charset getCharset() {
        return m_charset;
    }

    /**
     * <p>getMultilineIndicator</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilineIndicator() {
        return m_multilineIndicator;
    }

}

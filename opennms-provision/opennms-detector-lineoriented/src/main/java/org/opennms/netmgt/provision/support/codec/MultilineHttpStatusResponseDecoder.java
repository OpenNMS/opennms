/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.opennms.netmgt.provision.detector.simple.response.MultilineHttpResponse;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;


/**
 * <p>MultilineHttpStatusResponseDecoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MultilineHttpStatusResponseDecoder extends MultiLineDecoder {
    
    /**
     * <p>Constructor for MultilineHttpStatusResponseDecoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public MultilineHttpStatusResponseDecoder(final Charset charset) {
        super(charset, "\r\n");
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doDecode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        MultilineOrientedResponse response = (MultilineHttpResponse) session.getAttribute(CURRENT_RESPONSE);
        if(response == null) {
            response = new MultilineHttpResponse();
            session.setAttribute(CURRENT_RESPONSE, response);
        }
        // Remember the initial position.
        final int start = in.position();
        
        // Now find the first CRLF in the buffer.
        byte previous = 0;
        while (in.hasRemaining()) {
            final byte current = in.get();
            
            if (previous == '\r' && current == '\n') {
                // Remember the current position and limit.
                final int position = in.position();
                final int limit = in.limit();
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
    
    /** {@inheritDoc} */
    @Override
    protected boolean checkIndicator(final IoBuffer in) throws CharacterCodingException {
        final String line = in.getString(getCharset().newDecoder());
        return !line.equals("\r\n"); //line.substring(3, 4).equals(getMultilineIndicator());
    }

}

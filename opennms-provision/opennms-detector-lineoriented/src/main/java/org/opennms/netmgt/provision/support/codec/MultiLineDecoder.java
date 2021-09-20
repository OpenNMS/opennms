/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
    public MultiLineDecoder(final Charset charset, final String multilineIndicator) {
        setCharset(charset);
        m_multilineIndicator = multilineIndicator;
    }
    
    /** {@inheritDoc} */
    @Override
    protected boolean doDecode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        MultilineOrientedResponse response = (MultilineOrientedResponse) session.getAttribute(CURRENT_RESPONSE);
        if(response == null) {
            response = new MultilineOrientedResponse();
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

                    // If the multiline indicator is on this line then add the line to
                    // the response object and continue to process the next line
                    if(checkIndicator(in.slice())) { 
                        response.addLine(in.getString(getCharset().newDecoder()));
                    } else {
                        // Otherwise, add the current line and then submit the response
                        // to the ProtocolDecoderOutput instance
                        response.addLine(in.getString(getCharset().newDecoder()));
                        out.write(response);
                        session.removeAttribute(CURRENT_RESPONSE);
                        return true;
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
    protected boolean checkIndicator(final IoBuffer in) throws CharacterCodingException {
        final String line = in.getString(getCharset().newDecoder());
        return line.substring(3, 4).equals(getMultilineIndicator());
    }

    /**
     * <p>setCharset</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public void setCharset(final Charset charset) {
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

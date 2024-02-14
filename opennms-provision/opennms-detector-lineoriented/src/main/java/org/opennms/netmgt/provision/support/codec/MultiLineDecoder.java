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

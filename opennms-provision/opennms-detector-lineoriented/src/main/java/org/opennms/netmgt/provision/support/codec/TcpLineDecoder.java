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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;

public class TcpLineDecoder extends CumulativeProtocolDecoder {

    private Charset m_charset;
    
    public TcpLineDecoder(Charset charset) {
        setCharset(charset);
    }   
    
    private void setCharset(Charset charset) {
        m_charset = charset;
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
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
                    System.err.println("slice: " + in.slice());
                    out.write(parseCommand(in.slice()));
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
        
        out.write(parseCommand(in.slice()));
        
        return false;
    }
    
    protected Object parseCommand(IoBuffer in) {
        String outputStr = null;
        try{
            outputStr = in.getString(getCharset().newDecoder());
        }catch(CharacterCodingException e){
            outputStr = convertToString(in);
        }
        
        return new LineOrientedResponse(outputStr);
    }
    
    private String convertToString(IoBuffer in) {
        String cumulativeStr = "";
        while(in.hasRemaining()){
            byte current = in.get();
            char data[] = {(char) current};
            String temp = new String(data);
            cumulativeStr += temp;
        }
        return cumulativeStr;
    }

    public Charset getCharset() {
        return m_charset;
    }

}

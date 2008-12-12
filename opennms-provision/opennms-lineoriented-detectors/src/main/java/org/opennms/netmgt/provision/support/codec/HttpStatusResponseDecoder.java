package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.opennms.netmgt.provision.detector.HttpStatusResponse;

public class HttpStatusResponseDecoder extends LineOrientedDecoder {

    public HttpStatusResponseDecoder(Charset charset) {
        super(charset);
        
    }
    
    @Override
    protected Object parseCommand(IoBuffer in) throws CharacterCodingException {
        return new HttpStatusResponse(in.getString(getCharset().newDecoder()));
    }
}

package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.opennms.netmgt.provision.detector.simple.response.HttpStatusResponse;

/**
 * <p>HttpStatusResponseDecoder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class HttpStatusResponseDecoder extends LineOrientedDecoder {

    /**
     * <p>Constructor for HttpStatusResponseDecoder.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public HttpStatusResponseDecoder(Charset charset) {
        super(charset);
        
    }
    
    /** {@inheritDoc} */
    @Override
    protected Object parseCommand(IoBuffer in) throws CharacterCodingException {
        return new HttpStatusResponse(in.getString(getCharset().newDecoder()));
    }
}

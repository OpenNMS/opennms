package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * <p>HttpProtocolCodecFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class HttpProtocolCodecFactory implements ProtocolCodecFactory {
    
    private LineOrientedEncoder m_encoder;
    private HttpStatusResponseDecoder m_decoder;
    
    /**
     * <p>Constructor for HttpProtocolCodecFactory.</p>
     */
    public HttpProtocolCodecFactory() {
        this(Charset.defaultCharset());
    }
    
    /**
     * <p>Constructor for HttpProtocolCodecFactory.</p>
     *
     * @param charset a {@link java.nio.charset.Charset} object.
     */
    public HttpProtocolCodecFactory(final Charset charset) {
        m_encoder = new LineOrientedEncoder(charset);
        m_decoder = new HttpStatusResponseDecoder(charset);
    }
    
    /** {@inheritDoc} */
    public ProtocolDecoder getDecoder(final IoSession session) throws Exception {
        return m_decoder;
    }

    /** {@inheritDoc} */
    public ProtocolEncoder getEncoder(final IoSession session) throws Exception {
        return m_encoder;
    }

}

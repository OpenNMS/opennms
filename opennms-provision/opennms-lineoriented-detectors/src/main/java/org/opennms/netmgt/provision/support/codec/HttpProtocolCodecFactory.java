package org.opennms.netmgt.provision.support.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class HttpProtocolCodecFactory implements ProtocolCodecFactory {
    
    private LineOrientedEncoder m_encoder;
    private HttpStatusResponseDecoder m_decoder;
    
    public HttpProtocolCodecFactory() {
        this(Charset.defaultCharset());
    }
    
    public HttpProtocolCodecFactory(Charset charset) {
        m_encoder = new LineOrientedEncoder(charset);
        m_decoder = new HttpStatusResponseDecoder(charset);
    }
    
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return m_decoder;
    }

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return m_encoder;
    }

}

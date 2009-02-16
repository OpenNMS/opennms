package org.opennms.netmgt.provision.detector.simple;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.support.codec.MultilineOrientedCodecFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FtpDetector extends AsyncMultilineDetector {
    
    private static final String DEFAULT_SERVICE_NAME = "FTP";
    private static final int DEFAULT_PORT = 21;
    private String m_multilineIndicator = "-";
    
    /**
     * Default constructor
     */
    public FtpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     * 
     * @param serviceName
     * @param port
     */
    public FtpDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    public void onInit() {
        //setup the correct codec for this Detector
        setProtocolCodecFilter(new ProtocolCodecFilter( new MultilineOrientedCodecFactory( Charset.forName( "UTF-8"), getMultilineIndicator())));
        
        expectBanner(expectCodeRange(100, 600));
        send(request("quit"), expectCodeRange(100,600));
    }

    public void setMultilineIndicator(String multilineIndicator) {
        m_multilineIndicator = multilineIndicator;
    }

    public String getMultilineIndicator() {
        return m_multilineIndicator;
    }
}

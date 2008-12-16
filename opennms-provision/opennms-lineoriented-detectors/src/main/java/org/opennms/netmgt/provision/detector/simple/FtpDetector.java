package org.opennms.netmgt.provision.detector.simple;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.support.codec.MultilineOrientedCodecFactory;


public class FtpDetector extends AsyncMultilineDetector {
    
    private String m_multilineIndicator = "-";
    
    public FtpDetector() {
        super(21, 500, 3);
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

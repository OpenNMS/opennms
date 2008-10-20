package org.opennms.netmgt.provision.detector;

public class ImapDetector extends AbstractDetector {

    protected ImapDetector(int defaultPort, int defaultTimeout, int defaultRetries) {
        super(143, 5000, 0);
        
    }
    
    public void onInit(){
        expectBanner(startsWith("* OK "));
        addRequestReply("ONMSCAPSD LOGOUT\r\n", startsWith("ONMSCAPSD OK "));
    }

}

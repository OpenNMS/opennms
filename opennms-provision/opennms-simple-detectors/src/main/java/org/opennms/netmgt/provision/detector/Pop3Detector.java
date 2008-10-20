package org.opennms.netmgt.provision.detector;


public class Pop3Detector extends AbstractDetector {

    protected Pop3Detector() {
        super(110, 5000, 1);
    }

    public void onInit(){
        expectBanner(startsWith("+OK"));
        addRequestReply("QUIT", startsWith("+OK"));
    }

}

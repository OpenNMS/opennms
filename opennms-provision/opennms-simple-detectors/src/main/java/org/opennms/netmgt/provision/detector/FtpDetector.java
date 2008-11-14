package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.conversation.ClientConversation.ResponseValidator;

public class FtpDetector extends MultilineOrientedDetector {

    
    public FtpDetector() {
        super(21, 500, 3);
    }
    
    public void onInit() {
        expectBanner(expectCodeRange(100, 600));
        send(request("quit"), expectCodeRange(100,600));
        expectClose();
    }

    protected ResponseValidator<MultilineOrientedResponse> expectCodeRange(final int beginRange, final int endRange){
        return new ResponseValidator<MultilineOrientedResponse>() {
            
            public boolean validate(MultilineOrientedResponse response) {
                return response.expectedCodeRange(beginRange, endRange);
            }
            
        };
    }
}

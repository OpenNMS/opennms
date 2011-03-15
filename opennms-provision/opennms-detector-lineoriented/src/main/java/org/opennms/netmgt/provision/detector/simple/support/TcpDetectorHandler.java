package org.opennms.netmgt.provision.detector.simple.support;

import org.apache.mina.core.session.IoSession;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.BaseDetectorHandler;

public class TcpDetectorHandler extends BaseDetectorHandler<LineOrientedRequest, LineOrientedResponse> {

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if(!getConversation().hasBanner() && getConversation().getRequest() != null) {
            Object request = getConversation().getRequest();
            session.write(request);
       }else if(!getConversation().hasBanner() && getConversation().getRequest() == null) {
           getFuture().setServiceDetected(true);
           session.close(true);
       }
    }

}

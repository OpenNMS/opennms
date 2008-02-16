package org.opennms.ovapi;

import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.nnm.swig.OVsnmpSession;
import org.opennms.nnm.swig.SnmpCallback;
import org.opennms.nnm.swig.fd_set;
import org.opennms.nnm.swig.timeval;

public class TrapProcessingDaemon extends OVsDaemon {
    
    OVsnmpSession m_trapSession;
    
    public static void main(String[] args) {
        try {
            log("starting TrapProcessingDaemon");
            OVsDaemon daemon = new TrapProcessingDaemon();
            daemon.execute();
        } catch (Throwable t) {
            log("an exception was caught!", t);
        }
    }
    

    protected String onInit() {
        
        SnmpCallback trapCB = new SnmpCallback() {

            public void callback(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
                onEvent(reason, session, pdu);
            }
        };
        
        m_trapSession = OVsnmpSession.eventOpen("opennmsd", trapCB, ".*");

        return "TrapProcessingDaemon has initialized successfully.";
    }

    protected void onEvent(int reason, OVsnmpSession session, OVsnmpPdu pdu) {
        log("Recieved trap: reason = "+reason+" pdu.enterprise = "+pdu.getEnterpriseObjectId()+" generic = "+pdu.getGenericType()+" specific = "+pdu.getSpecificType());
        pdu.free();
    }

    protected String onStop() {
        
        
        m_trapSession.close();

        return "TrapProcessingDaemon has exited successfully.";
    }

    protected int getRetryInfo(fd_set fdset, timeval tm) {
        int maxSnmpFDs = OVsnmpSession.getRetryInfo(fdset, tm);
        int maxSuperFDs = super.getRetryInfo(fdset, tm);
        return Math.max(maxSnmpFDs, maxSuperFDs);
    }

    protected void processReads(fd_set fdset) {
        OVsnmpSession.read(fdset);
        super.processReads(fdset);
    }

    protected void processTimeouts() {
        OVsnmpSession.doRetry();
        super.processTimeouts();
    }

    
}

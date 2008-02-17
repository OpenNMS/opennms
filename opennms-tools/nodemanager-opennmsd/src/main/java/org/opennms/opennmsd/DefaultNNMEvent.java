package org.opennms.opennmsd;

import java.util.Date;

import org.apache.log4j.Logger;
import org.opennms.nnm.swig.OVsnmpPdu;
import org.opennms.ovapi.OVsnmpPduUtils;

public class DefaultNNMEvent implements NNMEvent {
    
    private static Logger log = Logger.getLogger(DefaultNNMEvent.class);

    public DefaultNNMEvent(OVsnmpPdu trap) {
        log.debug(OVsnmpPduUtils.toString(trap));
        trap.free();
    }

    public String getCategory() {
        return "Category";
    }

    public String getName() {
        return "Name";
    }

    public String getSeverity() {
        return "Severity";
    }

    public String getSourceAddress() {
        return "192.168.1.1";
    }

    public Date getTimeStamp() {
        return new Date();
    }
    
    


}

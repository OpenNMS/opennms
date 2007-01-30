package org.opennms.netmgt.correlation.drools;

import java.util.Arrays;
import java.util.List;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class DroolsCorrelationEngine extends AbstractCorrelationEngine implements InitializingBean {

    public void correlate(Event e) {
        throw new UnsupportedOperationException(
                "DroolsCorrelationEngine.correlate not yet implemented.");
    }

    public List<String> getInterestingEvents() {
        String[] ueis = {
                EventConstants.REMOTE_NODE_LOST_SERVICE_UEI,
                EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
        };
        return Arrays.asList(ueis);
    }

    public void afterPropertiesSet() throws Exception {
        
    }

}

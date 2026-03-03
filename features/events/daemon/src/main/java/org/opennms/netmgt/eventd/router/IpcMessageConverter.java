package org.opennms.netmgt.eventd.router;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

public class IpcMessageConverter {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    public IpcMessage convert(Event event) {
        String type = deriveMessageType(event.getUei());
        Map<String, String> parameters = new HashMap<>();
        if (event.getParmCollection() != null) {
            for (Parm parm : event.getParmCollection()) {
                if (parm.getValue() != null) {
                    parameters.put(parm.getParmName(), parm.getValue().getContent());
                }
            }
        }
        return new IpcMessage(
                type,
                event.getSource(),
                event.getTime() != null ? event.getTime().getTime() : System.currentTimeMillis(),
                event.getNodeid(),
                event.getInterface(),
                parameters
        );
    }

    private String deriveMessageType(String uei) {
        if (uei != null && uei.startsWith(INTERNAL_UEI_PREFIX)) {
            return uei.substring(INTERNAL_UEI_PREFIX.length());
        }
        return uei;
    }
}

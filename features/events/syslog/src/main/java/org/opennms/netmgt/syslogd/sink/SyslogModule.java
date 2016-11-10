package org.opennms.netmgt.syslogd.sink;

import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.syslogd.UDPMessageLogDTO;

public class SyslogModule extends AbstractXmlSinkModule<UDPMessageLogDTO> {

    public static final String MODULE_ID = "Syslog";

    public SyslogModule() {
        super(UDPMessageLogDTO.class);
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

}

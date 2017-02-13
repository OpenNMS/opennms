package org.opennms.features.minion.dominion;

import org.opennms.core.ipc.sink.xml.AbstractXmlIpcModule;
import org.opennms.minion.core.api.MinionIdentityDTO;

public class HeartbeatModule extends AbstractXmlIpcModule<MinionIdentityDTO> {

    public HeartbeatModule() {
        super(MinionIdentityDTO.class);
    }

    @Override
    public String getId() {
        return "Heartbeat";
    }

}

package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.AbstractInformationElementXmlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionInformationElementXmlProvider extends AbstractInformationElementXmlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MinionInformationElementXmlProvider.class);
    private InformationElementDatabase database;

    public MinionInformationElementXmlProvider(final TwinSubscriber twinSubscriber) {
        super(twinSubscriber);
    }

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }
}

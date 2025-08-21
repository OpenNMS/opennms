package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementXmlProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Optional;

public abstract class AbstractInformationElementXmlProvider implements InformationElementXmlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractInformationElementXmlProvider.class);
    public static final String TWIN_KEY = "ipfix-dot-d.config";

    protected InformationElementDatabase database;
    protected TwinSubscriber twinSubscriber;
    protected TwinPublisher.Session<IpfixDotD> twinSession;
    protected Closeable twinSubscription;

    public AbstractInformationElementXmlProvider(final TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
    }

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }

    protected void applyConfig(final InformationElementDatabase.Adder adder, final IpfixDotD ipfixDotD) {
        if (ipfixDotD == null) {
            return;
        }

        LOG.info("Applying new config...");

        adder.clear(getClass().getName());

        for(final IpfixElements ipfixElements : ipfixDotD.getIpfixElements()) {
            final long vendor = ipfixElements.getScope().getPen();

            for (final Element element : ipfixElements.getElements()) {
                final int id = element.getId();
                final String name = element.getName();
                final InformationElementDatabase.ValueParserFactory valueParserFactory = InformationElementProvider.TYPE_LOOKUP.get(element.getDataType());
                adder.add(Protocol.IPFIX, Optional.of(vendor), id, valueParserFactory, name, Optional.of(Semantics.DEFAULT), this.database, getClass().getName());
            }
        }
    }

    @Override
    public void load(InformationElementDatabase.Adder adder) {
        twinSubscription = twinSubscriber.subscribe(TWIN_KEY, IpfixDotD.class, (config) -> {
            applyConfig(adder, config);
        });
    }
}

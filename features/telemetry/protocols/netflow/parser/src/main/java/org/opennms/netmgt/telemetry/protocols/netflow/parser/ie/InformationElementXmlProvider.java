package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie;

public interface InformationElementXmlProvider extends InformationElementDatabase.Provider {
    @Override
    default void load(final InformationElementDatabase.Adder adder) {
    }

    @Override
    default InformationElementDatabase getDatabase() {
        return null;
    }

    @Override
    default void setDatabase(final InformationElementDatabase database) {
    }
}

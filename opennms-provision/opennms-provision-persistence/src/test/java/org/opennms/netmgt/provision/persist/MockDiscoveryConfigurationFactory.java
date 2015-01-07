package org.opennms.netmgt.provision.persist;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.api.DiscoveryConfigurationFactory;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;

public class MockDiscoveryConfigurationFactory implements DiscoveryConfigurationFactory {

    public MockDiscoveryConfigurationFactory() {
    }

    @Override
    public List<IPPollAddress> getURLSpecifics() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<IPPollRange> getRanges() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<IPPollAddress> getSpecifics() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isExcluded(InetAddress address) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getForeignSource(InetAddress address) {
        return null;
    }

    @Override
    public int getIntraPacketDelay() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Iterator<IPPollAddress> getExcludingInterator(Iterator<IPPollAddress> it) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Iterable<IPPollAddress> getConfiguredAddresses() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public long getRestartSleepTime() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public long getInitialSleepTime() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}

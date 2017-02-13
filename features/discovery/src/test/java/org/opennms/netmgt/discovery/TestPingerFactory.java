package org.opennms.netmgt.discovery;

import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;

public class TestPingerFactory implements PingerFactory {

    @Override
    public Pinger getInstance() {
        return new TestPinger();
    }

    @Override
    public Pinger getInstance(int tc, boolean allowFragmentation) {
        return new TestPinger();
    }

}

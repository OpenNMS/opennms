package org.opennms.netmgt.icmp.jna;

import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.Pinger;

public class JnaPingerFactory extends AbstractPingerFactory {

    @Override
    public Class<? extends Pinger> getPingerClass() {
        return JnaPinger.class;
    }
}

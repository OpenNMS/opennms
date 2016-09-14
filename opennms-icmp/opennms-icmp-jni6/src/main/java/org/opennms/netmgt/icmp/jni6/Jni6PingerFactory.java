package org.opennms.netmgt.icmp.jni6;

import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.Pinger;

public class Jni6PingerFactory extends AbstractPingerFactory {

    @Override
    public Class<? extends Pinger> getPingerClass() {
        return Jni6Pinger.class;
    }

}

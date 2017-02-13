package org.opennms.netmgt.icmp.jni;

import org.opennms.netmgt.icmp.AbstractPingerFactory;
import org.opennms.netmgt.icmp.Pinger;

public class JniPingerFactory extends AbstractPingerFactory {

    @Override
    public Class<? extends Pinger> getPingerClass() {
        return JniPinger.class;
    }

}

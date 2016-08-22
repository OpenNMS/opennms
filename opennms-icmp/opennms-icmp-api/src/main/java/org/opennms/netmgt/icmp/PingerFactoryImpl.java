package org.opennms.netmgt.icmp;

public class PingerFactoryImpl extends AbstractPingerFactory {
    @Override
    public Class<? extends Pinger> getPingerClass() {
        final String pingerClassName = System.getProperty("org.opennms.netmgt.icmp.pingerClass", "org.opennms.netmgt.icmp.jni6.Jni6Pinger");

        // If the default (0) DSCP pinger has already been initialized, use the
        // same class in case it's been manually overridden with a setInstance()
        // call (ie, in the Remote Poller)
        final Pinger defaultPinger = m_pingers.getIfPresent(1);
        if (defaultPinger != null) {
            return defaultPinger.getClass();
        }

        try {
            return Class.forName(pingerClassName).asSubclass(Pinger.class);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Unable to locate pinger class " + pingerClassName);
        }
    }

}

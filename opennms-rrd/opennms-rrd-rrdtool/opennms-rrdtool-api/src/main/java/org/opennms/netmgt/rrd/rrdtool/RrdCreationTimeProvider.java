package org.opennms.netmgt.rrd.rrdtool;

public class RrdCreationTimeProvider {
    public interface ProviderInterface {
        long currentTimeMillis();
    }

    public static ProviderInterface DEFAULT = new ProviderInterface() {
        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };

    private static ProviderInterface provider = DEFAULT;

    public static void setProvider(final ProviderInterface provider) {
        RrdCreationTimeProvider.provider = provider;
    }

    public static long currentTimeMillis() {
        if (RrdCreationTimeProvider.provider != null) {
            return provider.currentTimeMillis();
        } else {
            return DEFAULT.currentTimeMillis();
        }
    }
}

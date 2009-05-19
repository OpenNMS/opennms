package org.opennms.netmgt.threshd;

public class ThresholdingEventProxyFactory {

    private static ThresholdingEventProxyFactory instance = new ThresholdingEventProxyFactory();

    private ThreadLocal<ThresholdingEventProxy> eventProxyRef = new ThreadLocal<ThresholdingEventProxy>() {
        protected ThresholdingEventProxy initialValue() {
            return new ThresholdingEventProxy();
        }
    };

    private ThresholdingEventProxyFactory() {}

    public ThresholdingEventProxy getProxy() {
        return eventProxyRef.get();
    }

    public static ThresholdingEventProxyFactory getFactory() { return instance; }
}

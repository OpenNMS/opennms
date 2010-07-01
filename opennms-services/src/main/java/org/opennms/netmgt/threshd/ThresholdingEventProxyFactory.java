package org.opennms.netmgt.threshd;

/**
 * <p>ThresholdingEventProxyFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdingEventProxyFactory {

    private static ThresholdingEventProxyFactory instance = new ThresholdingEventProxyFactory();

    private ThreadLocal<ThresholdingEventProxy> eventProxyRef = new ThreadLocal<ThresholdingEventProxy>() {
        protected ThresholdingEventProxy initialValue() {
            return new ThresholdingEventProxy();
        }
    };

    private ThresholdingEventProxyFactory() {}

    /**
     * <p>getProxy</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdingEventProxy} object.
     */
    public ThresholdingEventProxy getProxy() {
        return eventProxyRef.get();
    }

    /**
     * <p>getFactory</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdingEventProxyFactory} object.
     */
    public static ThresholdingEventProxyFactory getFactory() { return instance; }
}

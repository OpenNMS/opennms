package org.opennms.netmgt.measurements.api;

/**
 * Every fetch strategy implementation is represented
 * by a provider, which can be used to determine whether or not
 * it can fetch measurements for a given RRD strategy.
 *
 * This allows us to determine which (fetch) strategy to use,
 * without needed to actually instantiate the classes,
 * since these may have dependencies which are not available
 * on the current class-path.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public interface MeasurementFetchStrategyProvider {

    /**
     * Returns a reference to an implementation of
     * {@link org.opennms.netmgt.measurements.api.MeasurementFetchStrategy}
     * that supports retrieving measurements for the given RRD strategy class.
     *
     * @param rrdStrategyClass canonical name of the {@link org.opennms.netmgt.rrd.RrdStrategy} implementation
     * @return null or a reference to a supported implementation
     */
    public Class<? extends MeasurementFetchStrategy> getStrategyClass(String rrdStrategyClass);

}

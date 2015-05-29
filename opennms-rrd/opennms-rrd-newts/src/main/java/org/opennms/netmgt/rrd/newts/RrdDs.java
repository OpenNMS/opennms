package org.opennms.netmgt.rrd.newts;

import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.newts.api.MetricType;

import com.google.common.base.Objects;

/**
 * Wrapper for RrdDataSource objects.
 *
 * @author jwhite
 */
public class RrdDs {

    private final MetricType m_metricType;

    private final String m_name;

    public RrdDs(RrdDataSource ds) {
        m_metricType = NewtsUtils.getMetricTypeFromRrdDataSource(ds);
        m_name = ds.getName();
    }

    public MetricType getMetricType() {
        return m_metricType;
    }

    public String getName() {
        return m_name;
    }

    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("name", m_name)
                .add("metricType", m_metricType)
                .toString();
    }
}

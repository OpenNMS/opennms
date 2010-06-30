/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.util.Comparator;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

package org.opennms.features.poller.remote.gwt.client;

import java.util.Comparator;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
class LocationSpecificStatusComparator implements Comparator<GWTLocationSpecificStatus> {
    /**
     * <p>compare</p>
     *
     * @param a a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus} object.
     * @param b a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationSpecificStatus} object.
     * @return a int.
     */
    public int compare(final GWTLocationSpecificStatus a, final GWTLocationSpecificStatus b) {
        return new CompareToBuilder()
            .append(a.getMonitoredService(), b.getMonitoredService())
            .append(
                 a.getLocationMonitor() == null? null : a.getLocationMonitor().getDefinitionName(),
                 b.getLocationMonitor() == null? null : b.getLocationMonitor().getDefinitionName()
            )
            .append(a.getPollTime(), b.getPollTime())
            .append(a.getLocationMonitor(), b.getLocationMonitor())
            .toComparison();
    }
}

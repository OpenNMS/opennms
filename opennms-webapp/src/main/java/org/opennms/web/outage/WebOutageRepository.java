package org.opennms.web.outage;

import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.filter.OutageCriteria;

/**
 * <p>WebOutageRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebOutageRepository {

    /**
     * <p>countMatchingOutages</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingOutages(OutageCriteria criteria);

    /**
     * <p>getOutage</p>
     *
     * @param OutageId a int.
     * @return a {@link org.opennms.web.outage.Outage} object.
     */
    public abstract Outage getOutage(int OutageId);

    /**
     * <p>getMatchingOutages</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     */
    public abstract Outage[] getMatchingOutages(OutageCriteria criteria);

    /**
     * <p>countMatchingOutageSummaries</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingOutageSummaries(OutageCriteria criteria);

    /**
     * <p>getMatchingOutageSummaries</p>
     *
     * @param criteria a {@link org.opennms.web.outage.filter.OutageCriteria} object.
     * @return an array of {@link org.opennms.netmgt.model.outage.OutageSummary} objects.
     */
    public abstract OutageSummary[] getMatchingOutageSummaries(OutageCriteria criteria);

    /**
     * Count the current number of nodes with outages.
     */
    public abstract int countCurrentOutages();

    /**
     * Get the current list of outages by node.
     * @param rows the number of outages to return.
     * @return
     */
    public abstract OutageSummary[] getCurrentOutages(int rows);
}

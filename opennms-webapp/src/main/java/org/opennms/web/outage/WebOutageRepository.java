package org.opennms.web.outage;

import org.opennms.web.outage.filter.OutageCriteria;

public interface WebOutageRepository {

    public abstract int countMatchingOutages(OutageCriteria criteria);

    public abstract Outage getOutage(int OutageId);

    public abstract Outage[] getMatchingOutages(OutageCriteria criteria);

}

package org.opennms.netmgt.dao;

import org.opennms.netmgt.model.DemandPoll;

public interface DemandPollDao {

	DemandPoll get(int resultId);

	void save(DemandPoll poll);

}

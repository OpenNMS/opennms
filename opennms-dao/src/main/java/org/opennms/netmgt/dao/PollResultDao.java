package org.opennms.netmgt.dao;

import org.opennms.netmgt.model.PollResult;

public interface PollResultDao {

	PollResult get(int resultId);

	void save(PollResult result);

}

package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.model.DemandPoll;

public class DemandPollDaoHibernate extends
		AbstractDaoHibernate<DemandPoll, Integer> implements DemandPollDao {

	public DemandPollDaoHibernate() {
		super(DemandPoll.class);
	}

}

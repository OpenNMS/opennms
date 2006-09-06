package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.AggregateStatusViewDao;
import org.opennms.netmgt.model.AggregateStatusView;

public class AggregateStatusViewDaoHibernate extends AbstractDaoHibernate<AggregateStatusView, Integer>
		implements AggregateStatusViewDao {

	public AggregateStatusViewDaoHibernate() {
		super(AggregateStatusView.class);
	}

	public AggregateStatusView findByName(String name) {
		return findUnique("from AggregateStatusView as a where a.name = ?", name);
	}


}

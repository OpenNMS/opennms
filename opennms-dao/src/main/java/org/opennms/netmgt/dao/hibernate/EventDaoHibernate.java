package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsEvent;

public class EventDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer>
		implements EventDao {

	public EventDaoHibernate() {
		super(OnmsEvent.class);
	}

}

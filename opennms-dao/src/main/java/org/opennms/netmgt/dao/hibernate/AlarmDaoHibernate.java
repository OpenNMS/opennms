package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmDaoHibernate extends AbstractDaoHibernate<OnmsAlarm, Integer> implements AlarmDao {
	
	public AlarmDaoHibernate() {
		super(OnmsAlarm.class);
	}

}

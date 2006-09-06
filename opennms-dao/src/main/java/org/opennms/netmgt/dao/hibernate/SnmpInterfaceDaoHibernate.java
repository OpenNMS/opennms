package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceDaoHibernate extends
		AbstractDaoHibernate<OnmsSnmpInterface, Integer> implements
		SnmpInterfaceDao {

	public SnmpInterfaceDaoHibernate() {
		super(OnmsSnmpInterface.class);
	}


}

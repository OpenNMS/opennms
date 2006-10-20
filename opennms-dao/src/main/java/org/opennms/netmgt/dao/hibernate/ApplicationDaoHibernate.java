package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.model.OnmsApplication;

public class ApplicationDaoHibernate extends AbstractDaoHibernate<OnmsApplication, Integer> implements ApplicationDao {

	public ApplicationDaoHibernate() {
		super(OnmsApplication.class);
	}

	public OnmsApplication findByName(String name) {
		return findUnique("from OnmsApplication as app where app.name = ?", name);
	}

}

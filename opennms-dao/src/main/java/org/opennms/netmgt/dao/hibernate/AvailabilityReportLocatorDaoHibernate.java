package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.opennms.netmgt.dao.AvailabilityReportLocatorDao;
import org.opennms.netmgt.model.AvailabilityReportLocator;

public class AvailabilityReportLocatorDaoHibernate extends AbstractDaoHibernate<AvailabilityReportLocator, Integer>
		implements AvailabilityReportLocatorDao {

	public AvailabilityReportLocatorDaoHibernate() {
		super(AvailabilityReportLocator.class);
	}

	public void delete(int id) {
		AvailabilityReportLocator locator = (AvailabilityReportLocator)getHibernateTemplate().get(AvailabilityReportLocator.class, id);
		super.delete(locator);
	}

	public Collection<AvailabilityReportLocator> findByCategory(String category) {
		return find("from AvailabilityReportLocator as a where a.category = ?", category);
	}


}

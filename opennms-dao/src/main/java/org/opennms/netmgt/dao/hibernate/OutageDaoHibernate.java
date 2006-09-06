package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.springframework.orm.hibernate3.HibernateCallback;

public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements
		OutageDao {

	public OutageDaoHibernate() {
		super(OnmsOutage.class);
	}

	public Integer currentOutageCount() {
		return queryInt("select count(*) from OnmsOutage as o where o.ifRegainedService is null");
	}

	public Collection<OnmsOutage> currentOutages() {
		return find("from OnmsOutage as o where o.ifRegainedService is null");
	}

	@SuppressWarnings("unchecked")
	public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
		return (Collection<OnmsOutage>)getHibernateTemplate().execute(new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createCriteria(OnmsOutage.class)
					.setFirstResult(offset)
					.setMaxResults(limit)
					.list();
			}
			
		});
	}

	public Collection<OnmsOutage> matchingCurrentOutages(ServiceSelector selector) {
    	Filter filter = new Filter();
    	Set<String> matchingIps = new HashSet<String>(filter.getIPList(selector.getFilterRule()));
    	Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());
    	
    	List<OnmsOutage> matchingOutages = new LinkedList<OnmsOutage>();
    	Collection<OnmsOutage> outages = currentOutages();
		for (OnmsOutage outage : outages) {
    		OnmsMonitoredService svc = outage.getMonitoredService();
    		if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) &&
    			matchingIps.contains(svc.getIpAddress())) {
    			
    			matchingOutages.add(outage);
    		}
			
		}
    	
    	
    	return matchingOutages;
	}

}

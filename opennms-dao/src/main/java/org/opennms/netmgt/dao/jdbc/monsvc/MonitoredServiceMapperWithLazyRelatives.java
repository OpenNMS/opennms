/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.dao.jdbc.outage.FindCurrentOutages;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class MonitoredServiceMapperWithLazyRelatives extends MonitoredServiceMapper {
	private DataSource m_dataSource;
	public MonitoredServiceMapperWithLazyRelatives(DataSource dataSource) {
		m_dataSource = dataSource;
	}
	protected void setCurrentOutages(OnmsMonitoredService svc) {
		final MonitoredServiceId id = new MonitoredServiceId(svc);
		LazySet.Loader outageLoader = new LazySet.Loader() {
		
		    public Set load() {
		        return new FindCurrentOutages(m_dataSource, id).findSet();
		    }
		    
		};
		
		svc.setCurrentOutages(new LazySet(outageLoader));
	}
}
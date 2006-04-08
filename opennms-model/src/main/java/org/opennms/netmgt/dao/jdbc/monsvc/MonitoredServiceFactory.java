package org.opennms.netmgt.dao.jdbc.monsvc;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.Factory;
import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceId;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class MonitoredServiceFactory extends Factory {
	
	public static void register(DataSource dataSource) {
        new MonitoredServiceFactory(dataSource);
	}

    public MonitoredServiceFactory() {
        super(OnmsMonitoredService.class);
    }

    public MonitoredServiceFactory(DataSource dataSource) {
        this();
        setDataSource(dataSource);
        afterPropertiesSet();
    }

	protected void assignId(Object obj, Object id) {
		OnmsMonitoredService svc = (OnmsMonitoredService)obj;
		MonitoredServiceId svcId = (MonitoredServiceId)id;
		
		IpInterfaceId ifaceId = svcId.getIpInterfaceId();
		
		OnmsIpInterface iface = (OnmsIpInterface)Cache.obtain(OnmsIpInterface.class, ifaceId);
		OnmsServiceType svcType = (OnmsServiceType)Cache.obtain(OnmsServiceType.class, svcId.getServiceId());
		
		svc.setIpInterface(iface);
		svc.setServiceType(svcType);
		
	}

	protected Object create() {
		return new LazyMonitoredService(getDataSource());
	}

}

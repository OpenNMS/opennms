package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.collectd.Service;

public class CollectionService {
	
	Service m_service;

	public CollectionService(Service svc) {
		m_service = svc;
	}

	public Service getService() {
		return m_service;
	}

	public String getName() {
		return m_service.getName();
	}

}

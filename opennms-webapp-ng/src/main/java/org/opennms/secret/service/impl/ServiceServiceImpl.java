package org.opennms.secret.service.impl;

import java.util.HashSet;

import org.opennms.secret.dao.NodeInterfaceDao;
import org.opennms.secret.dao.ServiceDao;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.service.NodeInterfaceService;
import org.opennms.secret.service.ServiceService;

public class ServiceServiceImpl implements ServiceService {
    private ServiceDao m_serviceDao;
    
    private String[] s_serviceNames = new String[] { "ICMP", "HTTP", "DNS", "SSH", "HTTPS" };
    
    public void setServiceDao(ServiceDao serviceDao) {
        m_serviceDao = serviceDao;
    }
    
    public HashSet getServices(NodeInterface iface) {
		HashSet services = new HashSet();
		for (int i = 0; i < 5; i++) {
			InterfaceService service = new InterfaceService();
			service.setId(new Long(i));
			service.setIpAddr("1.1.1." + (i + 1));
            service.setServiceName(s_serviceNames[i]);
			services.add(service);
		}
		return services;
	}

}

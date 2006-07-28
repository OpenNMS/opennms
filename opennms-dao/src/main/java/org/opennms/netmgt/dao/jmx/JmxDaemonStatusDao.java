package org.opennms.netmgt.dao.jmx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.model.ServiceDaemon;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jmx.access.MBeanProxyFactoryBean;

public class JmxDaemonStatusDao implements DaemonStatusDao {

	private MBeanServer mbeanServer;

	public void setMbeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	public Map<String, ServiceInfo> getCurrentDaemonStatus() {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> serviceInfo = new HashMap<String, ServiceInfo>();
		// go to the JMX Server and ask for all the MBeans...
		// ArrayList<MBeanServer> mbeans =
		// MBeanServerFactory.findMBeanServer(null);
		// get their names and corresponding status and plugit into Service Info

		Set<ObjectName> mBeanNames;
		try {
			mBeanNames = mbeanServer.queryNames(new ObjectName("opennms:*"), null);
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException(
					"Object name 'opennms:*' was malformed!", e);
		} catch (NullPointerException e) {
			throw new JmxObjectNameException("Object name param is null.", e);
		}

		for (ObjectName mBeanName : mBeanNames) {
			ServiceDaemon serviceDaemon = buildProxy(mBeanName);

			String name = serviceDaemon.getName();
			String status = serviceDaemon.status();
			serviceInfo.put(name, new ServiceInfo(name, status));
		}

		// Map the name of the service to ServiceInfo...
		// for testing adding a dummy service info node...
		serviceInfo.put("test", new ServiceInfo("test", "started"));
		return serviceInfo;
	}
	
	public Collection<ServiceInfo> getCurrentDaemonStatusColl() {
		// TODO Auto-generated method stub
		return this.getCurrentDaemonStatus().values();
	}
	
	public ServiceDaemon getServiceHandle(String service) {
		Set<ObjectName> mBeanNames;
		try {
			mBeanNames = mbeanServer.queryNames(new ObjectName("opennms:Name=" + service + ",*"), null);
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException("Object name 'opennms:Name=" + service
					+ ",*' was malformed!", e);
		} catch (NullPointerException e) {
			throw new JmxObjectNameException("Object name param is null.", e);
		}

		ObjectName mBeanName = (ObjectName) DataAccessUtils
				.requiredUniqueResult(mBeanNames);
		return buildProxy(mBeanName);
	}

	private ServiceDaemon buildProxy(ObjectName mBeanName) {
		MBeanProxyFactoryBean mBeanProxyFactoryBean = new MBeanProxyFactoryBean();

		try {
			mBeanProxyFactoryBean.setObjectName(mBeanName.getCanonicalName());
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException("Object name '"
					+ mBeanName.getCanonicalName() + "' was malformed!", e);
		}

		mBeanProxyFactoryBean.setProxyInterface(ServiceDaemon.class);

		mBeanProxyFactoryBean.afterPropertiesSet();
		return (ServiceDaemon) mBeanProxyFactoryBean.getObject();
	}
}

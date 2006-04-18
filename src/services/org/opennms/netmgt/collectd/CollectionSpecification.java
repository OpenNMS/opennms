package org.opennms.netmgt.collectd;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class CollectionSpecification {
	
	private Package m_package;
	private String m_svcName;
	private ServiceCollector m_collector;
	private Map m_parameters;

	public CollectionSpecification(Package pkg, String svcName, ServiceCollector collector) {
		m_package = pkg;
		m_svcName = svcName;
		m_collector = collector;
		initializeParameters();
	}

	public String getPackageName() {
		return m_package.getName();
	}
	
	public Package getPackage() {
		return m_package;
	}

	private String storeByIfAlias() {
		return getPackage().getStoreByIfAlias();
	}

	private String ifAliasComment() {
		return getPackage().getIfAliasComment();
	}

	private String storeFlagOverride() {
		return getPackage().getStorFlagOverride();
	}

	private String ifAliasDomain() {
		return getPackage().getIfAliasDomain();
	}

	private String storeByNodeId() {
		return getPackage().getStoreByNodeID();
	}

	private Service getService() {
		
		while (getPackage().enumerateService().hasMoreElements()) {
			Service svc = (Service) getPackage().enumerateService().nextElement();
			if (svc.getName().equalsIgnoreCase(m_svcName))
				return svc;
		}
		
		throw new RuntimeException("Service name not part of package!");
	}
	
	public String getServiceName() {
		return m_svcName;
	}

	public void setPackage(Package refreshedPackage) {
		m_package = refreshedPackage;
	}
	
	public long getInterval() {
		return getService().getInterval();
		
	}
	
	public String toString() {
		return m_svcName + '/' + m_package.getName();
	}

	public ServiceCollector getCollector() {
		return m_collector;
	}

	Map getPropertyMap() {
		return m_parameters;
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private boolean isTrue(String stg) {
		return stg.equalsIgnoreCase("yes") || stg.equalsIgnoreCase("on") || stg.equalsIgnoreCase("true");
	}

	private boolean isFalse(String stg) {
		return (stg.equalsIgnoreCase("no") || stg.equalsIgnoreCase("off") || stg.equalsIgnoreCase("false"));
	}

	private void initializeParameters() {
		Map m = new TreeMap();
		Enumeration ep = getService().enumerateParameter();
		while (ep.hasMoreElements()) {
			Parameter p = (Parameter) ep.nextElement();
			m.put(p.getKey(), p.getValue());
		}

		if(storeByIfAlias() != null && isTrue(storeByIfAlias())) {
			m.put("storeByIfAlias", "true");
			if(storeByNodeId() != null) {
				if(isTrue(storeByNodeId())) {
					m.put("storeByNodeID", "true");
				} else if(isFalse(storeByNodeId())) {
					m.put("storeByNodeID", "false");
				} else {
					m.put("storeByNodeID", "normal");
				}
			}
			if(ifAliasDomain() != null) {
				m.put("domain", ifAliasDomain());
			} else {
				m.put("domain", getPackageName());
			}
			if(storeFlagOverride() != null && isTrue(storeFlagOverride())) {
				m.put("storFlagOverride", "true");
			}
			m.put("ifAliasComment", ifAliasComment());
			if (log().isDebugEnabled())
				log().debug("ifAliasDomain = " + ifAliasDomain() + ", storeByIfAlias = " + storeByIfAlias() + ", storeByNodeID = " + storeByNodeId() + ", storFlagOverride = " + storeFlagOverride() + ", ifAliasComment = " + ifAliasComment());
		}
		
		m_parameters = m;
	
	}

	void initialize(CollectableService service) {
		m_collector.initialize(service, getPropertyMap());
	}

	void release(CollectableService service) {
		m_collector.release(service);
	}

	int collect(CollectableService service) {
		return getCollector().collect(service, eventProxy(), getPropertyMap());
	}

	private EventProxy eventProxy() {
		return new EventProxy() {
		    public void send(Event e) {
		        EventIpcManagerFactory.getIpcManager().sendNow(e);
		    }
	
		    public void send(Log log) {
		        EventIpcManagerFactory.getIpcManager().sendNow(log);
		    }
		};
	}

	
	
}

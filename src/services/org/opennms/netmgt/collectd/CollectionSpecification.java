package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class CollectionSpecification {
	
	private Package m_package;
	private String m_svcName;
	private ServiceCollector m_collector;
	private Map m_parameters;
	private Collection m_outageCalendars;
	
	public CollectionSpecification(Package pkg, String svcName, Collection outageCalendars, ServiceCollector collector) {
		m_package = pkg;
		m_svcName = svcName;
		m_collector = collector;
		m_outageCalendars = outageCalendars;
		initializeParameters();
	}

	public String getPackageName() {
		return m_package.getName();
	}
	
	private Package getPackage() {
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

	private void setPackage(Package refreshedPackage) {
		m_package = refreshedPackage;
	}
	
	public long getInterval() {
		return getService().getInterval();
		
	}
	
	public String toString() {
		return m_svcName + '/' + m_package.getName();
	}

	private ServiceCollector getCollector() {
		return m_collector;
	}

	private Map getPropertyMap() {
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

	public void initialize(CollectableService service) {
		m_collector.initialize(service, getPropertyMap());
	}

	public void release(CollectableService service) {
		m_collector.release(service);
	}

	public int collect(CollectableService service) {
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

	public boolean scheduledOutage(OnmsIpInterface iface) {
	
	    Category log = log();
	
		boolean outageFound = false;
	
	    PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();
	
	    // Iterate over the outage names defined in the interface's package.
	    // For each outage...if the outage contains a calendar entry which
	    // applies to the current time and the outage applies to this
	    // interface then break and return true. Otherwise process the
	    // next outage.
	    // 
		Iterator iter = getPackage().getOutageCalendarCollection().iterator();
	    while (iter.hasNext()) {
	        String outageName = (String) iter.next();
	
	        // Does the outage apply to the current time?
	        if (outageFactory.isCurTimeInOutage(outageName)) {
	            // Does the outage apply to this interface?
				if ((outageFactory.isNodeIdInOutage(iface.getNode().getId().longValue(), outageName)) ||
			(outageFactory.isInterfaceInOutage(iface.getIpAddress(), outageName)))
		{
					if (log.isDebugEnabled())
	                    log.debug("scheduledOutage: configured outage '" + outageName + "' applies, interface " + iface.getIpAddress() + " will not be collected for " + this);
	                outageFound = true;
	                break;
	            }
	        }
	    }
	
	    return outageFound;
	}

	public void refresh() {
		Package refreshedPackage=CollectdConfigFactory.getInstance().getPackage(getPackageName());
		if(refreshedPackage!=null) {
			setPackage(refreshedPackage);
		}
	}

	
	
}

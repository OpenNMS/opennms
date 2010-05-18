package org.opennms.netmgt.ticketer.rt;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.opennms.core.utils.ThreadCategory;

public class DefaultRtConfigDao {

	/**
	 * Retrieves the properties defined in the rt.properties file.
	 * 
	 * @return a <code>java.util.Properties</code> object containing rt plugin defined properties
	 * 
	 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
	 * 
	 */
	
	private Configuration getProperties() {
		
		String propsFile = new String(System.getProperty("opennms.home") + "/etc/rt.properties");
		
		log().debug("loading properties from: " + propsFile);
		
		Configuration config = null;
		
		try {
			config = new PropertiesConfiguration(propsFile);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return config;
	
	}
	
	/**
	 * Covenience logging.
	 * 
	 * @return a log4j Category for this class
	 */
	
	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String getUserName() {
		return getProperties().getString("rt.username");
	}

	
	String getPassword() {
		return getProperties().getString("rt.password");
	}
	
	String getQueue() {
		return getProperties().getString("rt.queue", "General");
	}
	
	@SuppressWarnings("unchecked")
	List<String> getValidClosedStatus() {
		
		return getProperties().getList("rt.validclosedstatus");
		
	}
	
	@SuppressWarnings("unchecked")
	List<Integer> getValidOpenStatus() {
		
		return getProperties().getList("rt.validopenstatus");
		
	}
	
	@SuppressWarnings("unchecked")
	List<String> getValidCancelledStatus() {
		
		return getProperties().getList("rt.validcancelledstatus");
		
	}
	
	String getOpenStatus() {
		return getProperties().getString("rt.openstatus", "open");
	}
	
	String getClosedStatus() { 
		return getProperties().getString("rt.closedstatus", "closed");
	}
	
	String getCancelledStatus() {
		return getProperties().getString("rt.cancelledstatus", "cancelled");
	}

	String getRequestor() {
        return getProperties().getString("rt.requestor");
    }
	
	String getBaseURL() {
	    return getProperties().getString("rt.baseURL");
	}
	
	int getTimeout() {
	    return getProperties().getInt("rt.timeout", 3000);
	}
	
	int getRetry() {
	    return getProperties().getInt("rt.retry",0);
	}
	
}

package org.opennms.netmgt.ticketer.remedy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.opennms.core.utils.LogUtils;

/**
 * <p>DefaultremedyConfigDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultRemedyConfigDao {

	Configuration m_config = null;
	/**
	 * Retrieves the properties defined in the remedy.properties file.
	 * 
	 * @param remedyTicketerPlugin
	 * @return a
	 *         <code>java.util.Properties object containing remedy plugin defined properties
	 */
	
	private Configuration getProperties() {
		if (m_config != null) return m_config;
		String propsFile = new String(System.getProperty("opennms.home") + "/etc/remedy.properties");
		
		LogUtils.debugf(this, "loading properties from: %s", propsFile);
		
		Configuration config = null;
		
		try {
			config = new PropertiesConfiguration(propsFile);
		} catch (final ConfigurationException e) {
		    LogUtils.debugf(this, e, "Unable to load properties from %s", propsFile);
		}
		m_config = config;
		return config;
	
	}
	
	/**
	 * <p>getUserName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUserName() {
		return getProperties().getString("remedy.username");
	}

	
	String getPassword() {
		return getProperties().getString("remedy.password");
	}

	String getAuthentication() {
		return getProperties().getString("remedy.authentication");
	}

	String getLocale() {
		return getProperties().getString("remedy.locale");		
	}
	
	String getTimeZone() {
		return getProperties().getString("remedy.timezone");
	}
	
	String getEndPoint() {
		return getProperties().getString("remedy.endpoint");
	}

	String getPortName() {
		return getProperties().getString("remedy.portname");
	}

	String getCreateEndPoint() {
		return getProperties().getString("remedy.createendpoint");
	}

	String getCreatePortName() {
		return getProperties().getString("remedy.createportname");
	}

	List<String> getTargetGroups() {
		List<String> targetGroups=new ArrayList<String>();
		if (getProperties().containsKey("remedy.targetgroups")) {
			for (String group: 	getProperties().getString("remedy.targetgroups").split(":")) {
				targetGroups.add(group);
			}
		}
		return targetGroups;
	}
	
	String getAssignedGroup() {
		return getProperties().getString("remedy.assignedgroup");
	}

	String getAssignedGroup(String targetGroup) {
		if (getProperties().containsKey("remedy.assignedgroup."+targetGroup))
			return getProperties().getString("remedy.assignedgroup."+targetGroup);
		return getAssignedGroup();
	}

	String getFirstName() {
		return getProperties().getString("remedy.firstname");
	}

	String getLastName() {
		return getProperties().getString("remedy.lastname");
	}

	String getServiceCI() {
		return getProperties().getString("remedy.serviceCI");
	}

	String getServiceCIReconID() {
		return getProperties().getString("remedy.serviceCIReconID");
	}
		
	String getAssignedSupportCompany() {
		return getProperties().getString("remedy.assignedsupportcompany");
	}

	String getAssignedSupportCompany(String targetGroup) {
		if (getProperties().containsKey("remedy.assignedsupportcompany."+targetGroup))
			return getProperties().getString("remedy.assignedsupportcompany."+targetGroup);
		return getAssignedSupportCompany();
	}

	String getAssignedSupportOrganization() {
		return getProperties().getString("remedy.assignedsupportorganization");
	}

	String getAssignedSupportOrganization(String targetGroup) {
		if (getProperties().containsKey("remedy.assignedsupportorganization."+targetGroup))
			return getProperties().getString("remedy.assignedsupportorganization."+targetGroup);
		return getAssignedSupportOrganization();
	}

	String getCategorizationtier1() {
		return getProperties().getString("remedy.categorizationtier1");
	}

	String getCategorizationtier2() {
		return getProperties().getString("remedy.categorizationtier2");
	}

	String getCategorizationtier3() {
		return getProperties().getString("remedy.categorizationtier3");
	}
	
	String getServiceType() {
		return getProperties().getString("remedy.serviceType");
	}
	
	String getReportedSource() {
		return getProperties().getString("remedy.reportedSource");
	}

	String getImpact() {
		return getProperties().getString("remedy.impact");
	}

	String getUrgency() {
		return getProperties().getString("remedy.urgency");
	}
	
	String getResolution() {
		return getProperties().getString("remedy.resolution");
	}

	String getReOpenStatusReason() {
		return getProperties().getString("remedy.reason.reopen");
	}
	
	String getResolvedStatusReason() {
		return getProperties().getString("remedy.reason.resolved");
	}
	
	String getCancelledStatusReason() {
		return getProperties().getString("remedy.reason.cancelled");
	}	
}

package org.opennms.netmgt.ticketer.otrs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class DefaultOtrsConfigDao {

	/**
	 * Retrieves the properties defined in the otrs.properties file.
	 * 
	 * @param otrsTicketerPlugin
	 * @return a
	 *         <code>java.util.Properties object containing otrs plugin defined properties
	 */
	
	private Configuration getProperties() {
		
		String propsFile = new String(System.getProperty("opennms.home") + "/etc/otrs.properties");
		
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
	
	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String getUserName() {
		return getProperties().getString("otrs.username");
	}

	
	String getPassword() {
		return getProperties().getString("otrs.password");
	}
	
	String getEndpoint() {
		return getProperties().getString("otrs.endpoint");
	}
	
	String getState() {
		return getProperties().getString("otrs.state");
	}
	
	Integer getOwnerID() {
		return getProperties().getInteger("otrs.ownerid", 1);
	}
	
	String getPriority() {
		return getProperties().getString("otrs.priority");
	}
	
	String getLock() {
		return getProperties().getString("otrs.lock");
	}
	
	String getQueue() {
		return getProperties().getString("otrs.queue");
	}
	
	String getArticleFrom() {
		return getProperties().getString("otrs.articlefrom");
	}
	
	String getArticleType() {
		return getProperties().getString("otrs.articletype");
	}
	
	String getArticleSenderType() {
		return getProperties().getString("otrs.articlesendertype");
	}
	
	String getArticleContentType() {
		return getProperties().getString("otrs.articlecontenttype");
	}
	
	String getArticleHistoryComment() {
		return getProperties().getString("otrs.articlehistorycomment");
	}
	
	String getArticleHistoryType() {
		return getProperties().getString("otrs.articlehistorytype");
	}
	
	@SuppressWarnings("unchecked")
	List<Integer> getValidClosedStateId() {
		
		List<String> closedStateId = getProperties().getList("otrs.validclosedstateid");
		return stringToInt(closedStateId);
		
	}
	
	@SuppressWarnings("unchecked")
	List<Integer> getValidOpenStateId() {
		
		List<String> openStateId = getProperties().getList("otrs.validopenstateid");
		return stringToInt(openStateId);
		
	}
	
	@SuppressWarnings("unchecked")
	List<Integer> getValidCancelledStateId() {
		
		List<String> cancelledStateId = getProperties().getList("otrs.validcancelledstateid");
		return stringToInt(cancelledStateId);
		
	}
	
	Integer getOpenStateId() {
		return getProperties().getInteger("otrs.openstateid", 1);
	}
	
	Integer getClosedStateId() {
		log().debug("getting closed state ID: " + getProperties().getInteger("otrs.closedstateid", 2)); 
		return getProperties().getInteger("otrs.closedstateid", 2);
	}
	
	Integer getCancelledStateId() {
		return getProperties().getInteger("otrs.cancelledstateid", 5);
	}
	
	String getDefaultUser() {
		return getProperties().getString("otrs.defaultuser");
	}
	
	private List<Integer> stringToInt(List<String> strings) {
		
		List<Integer> intList = new ArrayList<Integer>();
		
		for (String string : strings) {
			intList.add( Integer.parseInt(string));
		}
		
		return intList;
	}

	String getTicketOpenedMessage() {
		return getProperties().getString("otrs.ticketopenedmessage");
	}

	String getTicketClosedMessage() {
		return getProperties().getString("otrs.ticketclosedmessage");
	}

	String getTicketCancelledMessage() {
		return getProperties().getString("otrs.ticketcancelledmessage");
	}

	String getTicketUpdatedMessage() {
		return getProperties().getString("otrs.ticketupdatedmessage");
	}

	String getArticleUpdateSubject() {
		return getProperties().getString("otrs.articleupdatesubject");
	}
}

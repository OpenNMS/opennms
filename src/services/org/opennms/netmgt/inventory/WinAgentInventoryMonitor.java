/*
 * Created on 30-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;

import java.io.IOException;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.*;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import java.net.InetAddress;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.inventory.handler.*;
import org.opennms.core.utils.*;

/**
 * @author antonio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WinAgentInventoryMonitor implements InventoryMonitor {
	private String inventoryCategory  = null;
	private String newConfig = null;	
	// default WinAgentProvider port
	private static final int DEFAULT_PORT = 16341;
	// WinAgentPrivider success code
	private static final int SUCCESS_CODE = 200;
	private static final String RETRIEVE_NODEID_BY_INTERFACE =	"SELECT nodeId FROM ipInterface WHERE ipAddr = ?";
	private String retrieveMessage;
	private Map parameters;
	
	public int doRetrieve(NetworkInterface iface, Map parameters)
		throws IOException {
		
		this.parameters=parameters;	
		newConfig = null;
		// Get interface address from NetworkInterface
		//
		if (iface.getType() != NetworkInterface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

		// Process parameters
		//
		Category log = ThreadCategory.getInstance(getClass());
		log.debug("started.");

		InetAddress ipv4Addr = (InetAddress) iface.getAddress();
		String strPort = (String)parameters.get("port");
		int port=DEFAULT_PORT;
		if(strPort!=null){
			port = Integer.parseInt(strPort);
		}
		String path =  parameters.get("path").toString();
		if(path==null){
			log.error("Parameter 'path' not found.");
			return RETRIEVE_FAILURE;
		}
		java.sql.Connection dbConn = null;
		try {
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		} catch (SQLException s) {
			log.error("Unable to connect to DB");
			return RETRIEVE_FAILURE;
		} catch (Exception s) {
			log.error("Unable to connect to DB");
			return RETRIEVE_FAILURE;
		}

		int groupStatus = InventoryMonitor.RETRIEVE_FAILURE;

		URL u = null;
		try {

			u = new URL("http", ipv4Addr.getHostAddress(), port, path);

		} catch (MalformedURLException mex) {
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			log.error("Malformed URL for setting URL of WinAgentBroker");
			return RETRIEVE_FAILURE;
		}

		//int attempts = 0;
		WinAgentBroker m_WinAgentBroker = null;
		newConfig = null;
		boolean exit = false;
		/*for (responseCode = 0, newConfig = null;
			attempts < retry && groupStatus != GroupMonitor.RETRIEVE_SUCCESS;
			attempts++) {*/

		try { // sleeps timeout time if is an attempt greater then first
			/*if (attempts > 0) {
				Thread.sleep(timeout);
			}*/
			m_WinAgentBroker =
				new WinAgentBroker(WinAgentBroker.MEDIUM_SECURITY);
			m_WinAgentBroker.setURL(u);

			// connect for page download
			try {
				m_WinAgentBroker.connect();
				log.debug("WinAgentBroker connected. (" + iface + ")");
			} catch (IOException ioex) {
				groupStatus = RETRIEVE_FAILURE;
				log.debug("Unable to connect to WinAgentProvider.(" + iface + ")");
				exit = true;
			}

			if (!exit) {
				// try to download
				int responseCode = m_WinAgentBroker.getResponseCode();
				if (responseCode == SUCCESS_CODE) {
					newConfig = m_WinAgentBroker.getContent();
					log.debug("WinAgentBroker getcontent OK (" + iface + ") responseCode="+responseCode );
					groupStatus = RETRIEVE_SUCCESS;
					retrieveMessage =
						"Inventory successfully downloaded.<br>";
				} else {
					log.debug("WinAgentBroker getcontent FAILURE ("+ iface+") responseCode="+responseCode);
					groupStatus = RETRIEVE_FAILURE;
				}
			}
		}
		finally {
			if (m_WinAgentBroker != null) {
				m_WinAgentBroker.disconnect();
				log.debug("WinAgentBroker disconnected. (" + iface + ")");
			}
		}
		return groupStatus;


	}


	public String getData() throws IllegalStateException{
		XmlHandler xmlHnd = new XmlHandler();
		return xmlHnd.handle(newConfig,parameters);
	}
	
	
	/**
	 * Get the inventory category for the plug-in.
	 * 
	 * @return the inventory category for the plug-in.
	 *
	 **/
	public String getInventoryCategory(){
		return inventoryCategory;
	}
	public void setInventoryCategory(String invCategory){
		this.inventoryCategory=invCategory;
	}

	/**
	 * @return
	 */
	public String getRetrieveMessage() {
		return retrieveMessage;
	}

}

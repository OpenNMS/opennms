/*
 * Created on 30-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;

import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Category;
import org.opennms.netmgt.inventory.handler.*;
import org.opennms.core.utils.ThreadCategory;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * @author maurizio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SshInventoryMonitor implements InventoryMonitor {
	private String inventoryCategory  = null;
	private String newConfig = null;	
	private final int DEFAULT_TIMEOUT = 1;
	private static final String RETRIEVE_NODEID_BY_INTERFACE =	"SELECT nodeId FROM ipInterface WHERE ipAddr = ?";
	private String retrieveMessage;
	private Map parameters;
	
	public int doRetrieve(NetworkInterface iface, Map parameters)
		throws IOException {
		
		this.parameters=parameters;	
		newConfig = null;
		if (iface.getType() != NetworkInterface.TYPE_IPV4)
			throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");
		Category log = ThreadCategory.getInstance(getClass());
		log.debug("started.");

		InetAddress ipv4Addr = (InetAddress) iface.getAddress();
		String ipAddress = ipv4Addr.getHostAddress();
		String strPort = (String)parameters.get("port");

		String strTimout = (String)parameters.get("timeout");
		int timeout = DEFAULT_TIMEOUT;
		if(strTimout!=null){
			timeout = Integer.parseInt(strTimout);
		}
		String configurationFile =  parameters.get("plugin-conf-file").toString();
		if(configurationFile==null){
			log.error("Parameter 'plugin-conf-file' not found.");
			return RETRIEVE_FAILURE;
		}
		
		int groupStatus = RETRIEVE_SUCCESS;
		SshBroker sshbroker =null;
		try{
			sshbroker = new SshBroker(ipAddress,parameters,configurationFile);
		}catch(MarshalException m){
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			 log.error("Unable to parse plugin configuration file(" + m + ")");
		}
		catch(ValidationException v){
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			log.error("Unable to validate plugin configuration file(" + v + ")");
		}catch(IOException i){
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			log.error("Unable to parse plugin configuration file(" + i + ")");
		}
		try{
			newConfig = sshbroker.doCommand();
			log.debug(newConfig);
		}catch(IOException io){
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			log.error("Unable to download inventory.(" + io + ")");
			
		}catch(InterruptedException te){
			groupStatus = InventoryMonitor.RETRIEVE_FAILURE;
			log.error("Unable to download inventory. (" + te + ")");
		}
		return  groupStatus;
	}


	public String getData() throws IllegalStateException {
		SshHandler sshHnd = new SshHandler();
		return sshHnd.handle(newConfig,parameters);
	}
	
	
	/**
	 * 
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

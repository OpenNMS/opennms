/*
 * Created on 30-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;

import java.io.*;
import java.util.Map;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.*;
import java.net.InetAddress;

/**
 * @author maurizio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LdapInventoryMonitor implements InventoryMonitor {
	private String inventoryCategory  = null;
	private String newConfig = null;	
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
//		String strPort = (String)parameters.get("port");
//
//		String strTimout = (String)parameters.get("timeout");
//		int timeout = DEFAULT_TIMEOUT;
//		if(strTimout!=null){
//			timeout = Integer.parseInt(strTimout);
//		}
		int timeout = ParameterMap.getKeyedInteger(parameters,"timeout",0);
		int retry = ParameterMap.getKeyedInteger(parameters,"retry",0);
		String defName = parameters.get("definition-name").toString();
		if(defName==null){
			log.error("Parameter 'definition-name' not found.");
			return RETRIEVE_FAILURE;
		}
		String configurationFile =  parameters.get("plugin-conf-file").toString();
		if(configurationFile==null){
			log.error("Parameter 'plugin-conf-file' not found.");
			return RETRIEVE_FAILURE;
		}
		
		
		int groupStatus = RETRIEVE_SUCCESS;
		
		LdapSearcher l = new LdapSearcher(ipAddress,configurationFile,defName,timeout,retry);
		try{
			newConfig = l.getLdapInventory();

		}catch(Exception e){
			StackTraceElement [] ste = e.getStackTrace();
			String msg="";
			for(int i=0; i<ste.length; i++){
				msg+= ste[i];
			}
			log.error(e);
			log.error(e.getMessage());
			log.error(e.getLocalizedMessage());
			log.error(msg);
		
			groupStatus = RETRIEVE_FAILURE;
		}
		return  groupStatus;
	}


	public String getData() throws IllegalStateException {
		return newConfig;
	}
	
	
	/**
	 * NOT USED
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

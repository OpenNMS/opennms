/*
 * Creato il 27-ago-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.netmgt.inventory;

import java.util.Map;
import java.io.IOException;
/**
 * @author maurizio
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public interface InventoryMonitor {
	/**
	 * <P>The constant that defines a group as being in a normal
	 * state. If this is returned by the retrieve() method then the 
	 * framework will re-schedule the service for its next inventory using
	 * the standard uptime interval</P>
	 */
	public static final int		RETRIEVE_SUCCESS	= 1;
	
	/**
	 * <P>The constant that defines a group that is not working
	 * normally and should be scheduled using the downtime models.</P>
	 */
	public static final int		RETRIEVE_FAILURE	= 2;
	
	/** 
	 * <P>The constant that defines a configuration of a node is changed</P>
	 */
	public static final int		CONFIGURATION_CHANGED 	= 3;
	
	/** 
	 * <P>The constant that defines a configuration of a node isn't changed</P>
	 */
	public static final int		CONFIGURATION_NOT_CHANGED 	= 4;
	
	/** 
	 * <P>The constant that defines that a configuration was correctly saved</P>
	 */
	public static final int		CONFIGURATION_SAVED 	= 5;
	
	
	/** 
	* <P>The constant that defines that a configuration was not saved</P>
	*/
	public static final int		CONFIGURATION_NOT_SAVED 	= 6;
	
	
	/** 
	* <P>The constant that defines that a configuration for a node is downloaded for the first time</P>
	*/
	public static final int		FIRST_ACTIVE_CONFIGURATION_DOWNLOAD 	= 7;
	

	
	
	
	public abstract int doRetrieve(NetworkInterface iface, Map parameters) throws IOException;
	public abstract String getData()throws IllegalStateException;
	
	/**
	 * Get the inventory category for the plug-in.
	 * 
	 * @return the inventory category for the plug-in.
	 *
	 **/
	public String getInventoryCategory();
	public void setInventoryCategory(String inventoryCategory);
}

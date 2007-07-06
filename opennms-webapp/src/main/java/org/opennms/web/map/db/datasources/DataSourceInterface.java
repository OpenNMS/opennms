package org.opennms.web.map.db.datasources;

import java.util.Map;


/**
 * The interface DataSource provide a way to get data named like the input.
 * 
 * @author mmigliore
 *
 */
public interface DataSourceInterface {

	/**
	 * Gets the status of the element with id in input using params in input 
	 * @param velem
	 * @param params
	 * @return the status of velem, -1 if no status is found for velem
	 */
	public String getStatus(Object id);
	/**
	 * Gets the severity of the element with id in input using params in input 
	 * @param velem
	 * @param params
	 * @return the severity of velem, -1 if no severity is found for velem
	 */
	public String getSeverity(Object id);
	/**
	 * Gets the availability of the element with id in input using params in input 
	 * @param velem
	 * @param params
	 * @return the availability of velem, -1 if no availability is found for velem
	 */
	public double getAvailability(Object id);

}

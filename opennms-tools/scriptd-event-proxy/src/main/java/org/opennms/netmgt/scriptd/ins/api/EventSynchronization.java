package org.opennms.netmgt.scriptd.ins.api;
/**
 * Interface to use for triggering
 * a generic event synchronization
 *  
 * @author antonio
 *
 */
public interface EventSynchronization {

	/**
	 * Add an eventForwarder
	 * to forward Events
	 * 
	 * Multiple eventForwarders can be used to forward events
	 * 
	 * @param eventForwarder
	 */
	void addEventForwarder(EventForwarder eventForwarder);
	
	/**
	 * Criteria to be used to get the sync events
	 * 
	 * @param criteria
	 */
	void setCriteriaRestriction(String criteria);
	
	/**
	 * Calling this method get the synchronization
	 */
	void sync();

}

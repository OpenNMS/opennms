package org.opennms.features.poller.remote.gwt.client;

import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;


/**
 * This interface specifies the model functions that allow data access to the
 * set of known {@link Location} objects that have been transmitted from the
 * server to the GWT client code.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationManager {
    
    /**
     * <p>initialize</p>
     * @param statuses 
     * @param application 
     */
    public abstract void initialize(Set<Status> statuses);
	
	/**
	 * <p>addLocationManagerInitializationCompleteEventHandler</p>
	 *
	 * @param handler a {@link org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander} object.
	 */
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);
	/**
	 * <p>locationClicked</p>
	 */
	public void locationClicked();
	/**
	 * <p>applicationClicked</p>
	 */
	public void applicationClicked();
}

package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * <p>LocationStatusServiceAsync interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationStatusServiceAsync {
    /**
     * <p>start</p>
     *
     * @param anAsyncCallback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
     */
    void start(AsyncCallback<Void> anAsyncCallback);

	/**
	 * <p>getLocationInfo</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	void getLocationInfo(final String locationName, final AsyncCallback<LocationInfo> callback);

	/**
	 * <p>getLocationDetails</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	void getLocationDetails(final String locationName, final AsyncCallback<LocationDetails> callback);

	/**
	 * <p>getApplicationInfo</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	void getApplicationInfo(final String applicationName, final AsyncCallback<ApplicationInfo> callback);

	/**
	 * <p>getApplicationDetails</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @param callback a {@link com.google.gwt.user.client.rpc.AsyncCallback} object.
	 */
	void getApplicationDetails(final String applicationName, final AsyncCallback<ApplicationDetails> callback);
}

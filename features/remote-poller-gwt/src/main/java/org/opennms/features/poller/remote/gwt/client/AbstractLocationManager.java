package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;


public abstract class AbstractLocationManager implements LocationManager {

	public enum State {
		UNINITIALIZED,
		APIKEY_LOADING,
		APIKEY_LOADED,
		MAP_API_LOADING,
		MAP_API_LOADED,
		MARKER_API_LOADING,
		MARKER_API_LOADED,
		ICON_API_LOADING,
		ICON_API_LOADED,
		EVENT_BACKEND_LOADING,
		FINISHED
	}

	public class InitializationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InitializationException() {
			super();
		}

		public InitializationException(String message, Throwable cause) {
			super(message, cause);
		}

		public InitializationException(String message) {
			super(message);
		}

		public InitializationException(Throwable cause) {
			super(cause);
		}
		
	}

	public abstract class InitializationCommand implements IncrementalCommand {
		protected State m_currentState = State.UNINITIALIZED;

		public boolean execute() {
			try {
				switch(m_currentState) {
					case UNINITIALIZED:
						initializeApiKey();
						m_currentState = State.APIKEY_LOADING;
						return true;
					case APIKEY_LOADING:
						if (m_apiKey != null) {
							m_currentState = State.APIKEY_LOADED;
						}
						return true;
					case APIKEY_LOADED:
						loadMapApi();
						m_currentState = State.MAP_API_LOADING;
						return true;
					case MAP_API_LOADING:
						if (mapApiLoaded()) {
							m_currentState = State.MAP_API_LOADED;
						}
						return true;
					case MAP_API_LOADED:
						loadMarkerApi();
						m_currentState = State.MARKER_API_LOADING;
						return true;
					case MARKER_API_LOADING:
						if (markerApiLoaded()) {
							m_currentState = State.MARKER_API_LOADED;
						}
						return true;
					case MARKER_API_LOADED:
						loadIconApi();
						m_currentState = State.ICON_API_LOADING;
						return true;
					case ICON_API_LOADING:
						if (iconApiLoaded()) {
							m_currentState = State.ICON_API_LOADED;
						}
						return true;
					case ICON_API_LOADED:
						loadEventBackend();
						m_currentState = State.EVENT_BACKEND_LOADING;
						return true;
					case EVENT_BACKEND_LOADING:
						if (eventBackendLoaded()) {
							m_currentState = State.FINISHED;
						}
						return true;
					case FINISHED:
						finished();
						return false;
				}
			} catch (InitializationException e) {
				throw new RuntimeException(e);
			}
			return false;
		}

		/**
		 * Override this to put whatever initialization code is required to load the map API.
		 */
		protected void loadMapApi() throws InitializationException {}
		/**
		 * Override this to notify the location manager when map API loading has completed.
		 * Additionally, any post-loading configuration should be performed here.
		 * @return true if the map API has completed loading
		 */
		protected boolean mapApiLoaded() throws InitializationException { return true; }
		/**
		 * Override this to put whatever initialization code is required to load a marker API.
		 */
		protected void loadMarkerApi() throws InitializationException {}
		/**
		 * Override this to notify the location manager that the marker API loading has completed.
		 * Additionally, any post-loading configuration should be performed here.
		 * @return true if the marker API has completed loading
		 */
		protected boolean markerApiLoaded() throws InitializationException { return true; }
		/**
		 * Override this to put whatever initialization code is required to load an icon API.
		 */
		protected void loadIconApi() throws InitializationException {}
		/**
		 * Override this to notify the location manager that the icon API loading has completed.
		 * Additionally, any post-loading configuration should be performed here.
		 * @return true if the icon API has completed loading
		 */
		protected boolean iconApiLoaded() throws InitializationException { return true; }
		/**
		 * Override this to put whatever initialization code is required to load the event backend.
		 */
		protected void loadEventBackend() throws InitializationException {}
		/**
		 * Override this to notify the location manager that the event backend loading has completed.
		 * Additionally, any post-loading configuration should be performed here.
		 * @return true if the event backend has completed loading
		 */
		protected boolean eventBackendLoaded() throws InitializationException { return true; }
		/**
		 * Override this 
		 * @throws InitializationException
		 */
		protected void finished() throws InitializationException {}
	}

	protected final HandlerManager m_eventBus;
	
	private String m_apiKey;
	private static final Set<String> m_locationsUpdating = new HashSet<String>();

	protected final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);

	public AbstractLocationManager(HandlerManager eventBus) {
		m_eventBus = eventBus;
	}

	public abstract void initialize();
	
	public abstract void updateLocation(Location location);
	public abstract void removeLocation(Location location);

	public void updateLocations(Collection<Location> locations) {
		for (Location location : locations) {
			if (location == null) continue;
			locationUpdateInProgress(location);
		}

		for (Location location : locations) {
			if (location == null) continue;
			updateLocation(location);
		}
	}
	
	public void removeLocations(Collection<Location> locations) {
		for (Location location : locations) {
			if (location == null) continue;
			locationUpdateInProgress(location);
		}
		
		for (Location location : locations) {
			if (location == null) continue;
			removeLocation(location);
		}
		
		m_eventBus.fireEvent(new LocationsUpdatedEvent(this));
	}

	public abstract void updateComplete();

	public abstract Location getLocation(int index);
	public abstract List<Location> getAllLocations();
	public abstract List<Location> getLocations(int startIndex, int maxRows);
	public abstract List<Location> getVisibleLocations();
	public abstract void selectLocation(String locationName);
	public abstract void fitToMap();

	public abstract void reportError(String message, Throwable throwable);

	protected void setApiKey(String key) {
		m_apiKey = key;
	}

	protected String getApiKey() {
		return m_apiKey;
	}

	protected void initializeApiKey() {
		m_remoteService.getApiKey(new AsyncCallback<String>() {
			public void onFailure(Throwable throwable) {
				Log.debug("failed to get API key", throwable);
			}

			public void onSuccess(String key) {
				Log.debug("got API key: " + key);
				setApiKey(key);
			}
		});
	}

	protected void locationUpdateInProgress(Location location) {
		m_locationsUpdating.add(location.getName());
	}

	protected void locationUpdateComplete(final Location location) {
		m_locationsUpdating.remove(location.getName());
	}

	protected boolean isLocationUpdateInProgress() {
		return m_locationsUpdating.size() > 0;
	}
	
}

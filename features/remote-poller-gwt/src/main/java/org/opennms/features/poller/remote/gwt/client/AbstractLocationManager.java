package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DeferredCommand;


public abstract class AbstractLocationManager implements LocationManager {

	protected final HandlerManager m_eventBus;
	
	private static final Set<String> m_locationsUpdating = new HashSet<String>();
	private final Application m_application;

	private final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);

	public AbstractLocationManager(final Application application, final HandlerManager eventBus) {
		m_application = application;
		m_eventBus = eventBus;
	}
	
    public void initialize() {
        DeferredCommand.addCommand(new InitializationCommand(this, createFinisher(), createDataLoaders()));
    }

    private Runnable createFinisher() {
        return new Runnable() {
            public void run() {
                initializationComplete();
            }
        };
    }
    
    protected DataLoader[] createDataLoaders() {
        return new DataLoader[] {
            new DataLoader() {
                @Override
                public void onLoaded() {
                    initializeMapWidget();
                }
            },
             new EventServiceInitializer(this)
        };
    }
    
    protected abstract void initializeMapWidget();
    
    protected void initializationComplete() {
        getApplication().finished();
    }
    

	
	public void updateLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = location.getLatLng();
		if (latLng == null) {
			Log.warn("no lat/lng for this location");
		} else {
			updateMarker(location);
		}
	}

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

	public abstract List<Location> getAllLocations();
	public abstract List<Location> getVisibleLocations();
	public abstract void selectLocation(String locationName);
	public abstract void fitToMap();

	public abstract void reportError(String message, Throwable throwable);


	protected void locationUpdateInProgress(Location location) {
		m_locationsUpdating.add(location.getName());
	}

	protected void locationUpdateComplete(final Location location) {
		m_locationsUpdating.remove(location.getName());
	}

	protected boolean isLocationUpdateInProgress() {
		return m_locationsUpdating.size() > 0;
	}
	
	protected Application getApplication() {
		return m_application;
	}
	
	abstract protected void updateMarker(Location location);

    protected LocationStatusServiceAsync getRemoteService() {
        return m_remoteService;
    }
    
    
    
 }

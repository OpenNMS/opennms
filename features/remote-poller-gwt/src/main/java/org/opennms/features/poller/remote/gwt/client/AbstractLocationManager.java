package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;


public abstract class AbstractLocationManager implements LocationManager {

	protected final HandlerManager m_eventBus;

	private static final Set<String> m_locationsUpdating = new HashSet<String>();
	private final HandlerManager m_handlerManager = new HandlerManager(this);

	private final LocationStatusServiceAsync m_remoteService = GWT.create(LocationStatusService.class);

	public AbstractLocationManager(final HandlerManager eventBus) {
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
        m_handlerManager.fireEvent(new LocationManagerInitializationCompleteEvent());
    }

    public void updateLocation(final LocationInfo info) {
    	if (info == null) return;
    	final BaseLocation l = new BaseLocation();
    	l.setLocationInfo(info);
    	updateMarker(l);
    }

	public void updateLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = location.getLocationInfo().getLatLng();
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
		m_locationsUpdating.add(location.getLocationInfo().getName());
	}

	protected void locationUpdateComplete(final Location location) {
		m_locationsUpdating.remove(location.getLocationInfo().getName());
	}

	protected boolean isLocationUpdateInProgress() {
		return m_locationsUpdating.size() > 0;
	}

	abstract protected void updateMarker(Location location);

    protected LocationStatusServiceAsync getRemoteService() {
        return m_remoteService;
    }
    
    public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler) {
        m_handlerManager.addHandler(LocationManagerInitializationCompleteEvent.TYPE, handler);
    };
    
    protected void displayDialog(final String title, final String contents) {
    	final DialogBox db = new DialogBox();
    	db.setAutoHideEnabled(true);
    	db.setModal(true);
    	db.setText(title);
    	db.setWidget(new Label(contents, true));
    	db.show();
    }

 }

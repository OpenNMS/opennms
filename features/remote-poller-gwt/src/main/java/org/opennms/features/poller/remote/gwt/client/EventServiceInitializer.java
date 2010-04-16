/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;


// import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;

final class EventServiceInitializer extends InitializationCommand.DataLoader {
    /**
     * 
     */
    private final DefaultLocationManager m_locationManager;

    /**
     * @param mapquestInitialization
     */
    EventServiceInitializer( final DefaultLocationManager locationManager ) {
        m_locationManager = locationManager;
    }

    @Override
    public void load() {
        LocationListener locationListener = new DefaultLocationListener(m_locationManager);
        final RemoteEventService eventService = RemoteEventServiceFactory.getInstance().getRemoteEventService();
        eventService.addListener(RemotePollerPresenter.LOCATION_EVENT_DOMAIN, locationListener);
        eventService.addListener(null, locationListener);
        
        m_locationManager.getRemoteService().start(new AsyncCallback<Void>() {
            public void onFailure(Throwable throwable) {
                // Log.debug("unable to start location even service backend", throwable);
                Window.alert("unable to start location event service backend: " + throwable.getMessage());
                throw new InitializationException("remote service start failed", throwable);
            }
        
            public void onSuccess(Void voidArg) {
                setLoaded();
            }
        });
    }
}

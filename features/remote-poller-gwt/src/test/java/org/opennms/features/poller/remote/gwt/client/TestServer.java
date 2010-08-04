package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public class TestServer extends AbstractTestServer {

    private RemoteEventListener m_userSpecificListener;
    private RemoteEventListener m_domainListener;

    @Override
    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener) {
        if(aDomain == null) {
            m_userSpecificListener = aRemoteListener;
        }else {
            m_domainListener = aRemoteListener;
        }
    }

    @Override
    public void start(AsyncCallback<Void> anAsyncCallback) {
        anAsyncCallback.onSuccess(null);
    }

    public void sendUserSpecificEvent( MapRemoteEvent remoteEvent ) {
        m_userSpecificListener.apply(remoteEvent);
    }
    
    public void sendDomainEvent( MapRemoteEvent remoteEvent) {
        m_domainListener.apply(remoteEvent);
    }

}

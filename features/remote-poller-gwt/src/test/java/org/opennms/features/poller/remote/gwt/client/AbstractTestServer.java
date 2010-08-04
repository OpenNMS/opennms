package org.opennms.features.poller.remote.gwt.client;

import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.filter.EventFilter;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;
import de.novanic.eventservice.client.event.listener.unlisten.UnlistenEvent;
import de.novanic.eventservice.client.event.listener.unlisten.UnlistenEventListener;
import de.novanic.eventservice.client.event.listener.unlisten.UnlistenEventListener.Scope;

public abstract class AbstractTestServer implements RemoteEventService,
        LocationStatusServiceAsync {

    public void start(AsyncCallback<Void> anAsyncCallback) {
        throw new UnsupportedOperationException("start is not implemented");
    }

    public void getLocationInfo(String locationName, AsyncCallback<LocationInfo> callback) {
        throw new UnsupportedOperationException("getLocationInfo is not implemented");
    }

    public void getLocationDetails(String locationName,
            AsyncCallback<LocationDetails> callback) {
        throw new UnsupportedOperationException(
                "getLocationDetails is not implemented");
    }

    public void getApplicationInfo(String applicationName,
            AsyncCallback<ApplicationInfo> callback) {
        throw new UnsupportedOperationException(
                "getApplicationInfo is not implemented");
    }

    public void getApplicationDetails(String applicationName,
            AsyncCallback<ApplicationDetails> callback) {
        throw new UnsupportedOperationException(
                "getApplicationDetails is not implemented");
    }

    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener) {
        throw new UnsupportedOperationException("addListener is not implemented");
    }

    public void addListener(Domain aDomain,
            RemoteEventListener aRemoteListener, AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addListener is not implemented");
    }

    public void addListener(Domain aDomain,
            RemoteEventListener aRemoteListener, EventFilter anEventFilter) {
        throw new UnsupportedOperationException(
                "addListener is not implemented");
    }

    public void addListener(Domain aDomain,
            RemoteEventListener aRemoteListener, EventFilter anEventFilter,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addListener is not implemented");
    }

    public void addUnlistenListener(
            UnlistenEventListener anUnlistenEventListener,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addUnlistenListener is not implemented");
    }

    public void addUnlistenListener(Scope anUnlistenScope,
            UnlistenEventListener anUnlistenEventListener,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addUnlistenListener is not implemented");
    }

    public void addUnlistenListener(
            UnlistenEventListener anUnlistenEventListener,
            UnlistenEvent anUnlistenEvent, AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addUnlistenListener is not implemented");
    }

    public void addUnlistenListener(Scope anUnlistenScope,
            UnlistenEventListener anUnlistenEventListener,
            UnlistenEvent anUnlistenEvent, AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "addUnlistenListener is not implemented");
    }

    public void removeListener(Domain aDomain,
            RemoteEventListener aRemoteListener) {
        throw new UnsupportedOperationException(
                "removeListener is not implemented");
    }

    public void removeListener(Domain aDomain,
            RemoteEventListener aRemoteListener, AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeListener is not implemented");
    }

    public void registerEventFilter(Domain aDomain, EventFilter anEventFilter) {
        throw new UnsupportedOperationException(
                "registerEventFilter is not implemented");
    }

    public void registerEventFilter(Domain aDomain, EventFilter anEventFilter,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "registerEventFilter is not implemented");
    }

    public void deregisterEventFilter(Domain aDomain) {
        throw new UnsupportedOperationException(
                "deregisterEventFilter is not implemented");
    }

    public void deregisterEventFilter(Domain aDomain,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "deregisterEventFilter is not implemented");
    }

    public boolean isActive() {
        throw new UnsupportedOperationException("isActive is not implemented");
    }

    public Set<Domain> getActiveDomains() {
        throw new UnsupportedOperationException(
                "getActiveDomains is not implemented");
    }

    public List<RemoteEventListener> getRegisteredListeners(Domain aDomain) {
        throw new UnsupportedOperationException(
                "getRegisteredListeners is not implemented");
    }

    public void removeListeners() {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeListeners(AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeListeners(Set<Domain> aDomains) {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeListeners(Set<Domain> aDomains,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeListeners(Domain aDomain) {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeListeners(Domain aDomain, AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeListeners is not implemented");
    }

    public void removeUnlistenListener(
            UnlistenEventListener anUnlistenEventListener,
            AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeUnlistenListener is not implemented");
    }

    public void removeUnlistenListeners(AsyncCallback<Void> aCallback) {
        throw new UnsupportedOperationException(
                "removeUnlistenListeners is not implemented");
    }

}

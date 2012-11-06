package org.opennms.features.topology.app.internal.gwt.client.presenter;

import com.google.gwt.event.shared.EventBus;
import com.vaadin.Application;


public interface IPresenter<V, E extends EventBus> {
    
    public void setEventBus(E eventBus);

    public E getEventBus();

    public void setView(V view);

    public V getView();

    public void bind();

    public void bindIfNeeded();

    public void setApplication(Application application);

    public Application getApplication();

    public void showNotification(String caption);

    public void showNotification(String caption, String description, int type);
}

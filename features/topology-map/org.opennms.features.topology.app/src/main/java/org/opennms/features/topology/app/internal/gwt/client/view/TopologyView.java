package org.opennms.features.topology.app.internal.gwt.client.view;

import com.google.gwt.user.client.ui.Widget;

public interface TopologyView<T> {

    public interface Presenter<T>{
        T getViewRenderer();
    }
    
    void setViewRenderer(T viewRenderer);
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
}

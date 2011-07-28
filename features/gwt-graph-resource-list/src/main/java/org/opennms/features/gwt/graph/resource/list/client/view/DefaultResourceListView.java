package org.opennms.features.gwt.graph.resource.list.client.view;

public interface DefaultResourceListView<T> extends ResourceListView<T> {
    
    public interface Presenter<T> extends ResourceListView.Presenter<T>{
        
    }
    
    void setPresenter(Presenter<T> presenter);
}

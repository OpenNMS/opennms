package org.opennms.features.gwt.graph.resource.list.client.presenter;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window.Location;

public class KscReportListPresenter extends DefaultResourceListPresenter {


    public KscReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopup searchPopup, JsArray<ResourceListItem> dataList) {
        super(view, searchPopup, dataList);
    }
    

    @Override
    public void onResourceItemSelected() {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setHost(Location.getHost());
        urlBuilder.setPath("opennms/KSC/customView.htm");
        urlBuilder.setParameter("type", getView().getSelectedResource().getType());
        urlBuilder.setParameter("report", "" + getView().getSelectedResource().getId());
        
        Location.assign(urlBuilder.buildString());
    }
    

}

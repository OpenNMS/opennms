package org.opennms.features.gwt.graph.resource.list.client.presenter;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window.Location;

public class KscReportListPresenter extends DefaultResourceListPresenter {


    public KscReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopup searchPopup, JsArray<ResourceListItem> dataList, String baseUrl) {
        super(view, searchPopup, dataList, baseUrl);
    }
    

    @Override
    public void onResourceItemSelected() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseUrl() + "/KSC/customView.htm");
        urlBuilder.append("?type=" + getView().getSelectedResource().getType());
        urlBuilder.append("&report=" + getView().getSelectedResource().getId());
        
        Location.assign(urlBuilder.toString());
    }
    

}

package org.opennms.features.gwt.graph.resource.list.client.presenter;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window.Location;

public class KscReportListPresenter extends DefaultResourceListPresenter {


    public KscReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopup searchPopup, JsArray<ResourceListItem> dataList) {
        super(view, searchPopup, dataList);
    }
    

    @Override
    public void onResourceItemSelected() {
        Location.assign("KSC/customView.htm?type=" + getView().getSelectedResource().getType() +"&report=" + getView().getSelectedResource().getId());
    }
    

}

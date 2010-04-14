package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PageableLocationList extends Composite {

    private static PageableLocationListUiBinder uiBinder = GWT.create(PageableLocationListUiBinder.class);

    interface PageableLocationListUiBinder extends UiBinder<Widget, PageableLocationList> {}
    
    private class LocationInfoDisplay extends Widget{
        
        Label m_label = new Label();
        
        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_label.getElement());
        }

        public LocationInfoDisplay(LocationInfo locationInfo) {
            m_label.setText(locationInfo.getName());
        }
        
    }
    
    private static final int TOTAL_LOCATIONS = 20;
    
    @UiField FlexTable dataList;
    @UiField FlowPanel pagingControls;
    
    public PageableLocationList() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void updateList(List<Location> locations) {
        dataList.removeAllRows();
        
        int count = 0;
        for(Location location : locations) {
            dataList.setWidget(count, 0, new LocationInfoDisplay(location.getLocationInfo()));
            
            if(count > TOTAL_LOCATIONS - 1) {
                break;
            }
            
            count++;
        }
    }

}

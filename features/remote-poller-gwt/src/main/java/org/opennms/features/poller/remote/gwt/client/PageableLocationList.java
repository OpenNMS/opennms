package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;


public class PageableLocationList extends PageableList {
    
    private ArrayList<? extends LocationInfo> m_locations;

    private class LocationInfoDisplay extends Widget{
        
        Image m_icon = new Image();
        Label m_nameLabel = new Label();
        Label m_areaLabel = new Label();
        Label m_statusLabel = new Label();
        
        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_areaLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());
        }
        
        @Override
        protected void onLoad() {
            resizeToFit();
        }
        
        protected void resizeToFit() {
            int calculatedHeight = m_nameLabel.getOffsetHeight() + m_statusLabel.getOffsetHeight();
            int newHeight = calculatedHeight > 60 ? calculatedHeight : 60;
            setHeight(Integer.toString(newHeight + 2));
        }

        public LocationInfoDisplay(LocationInfo locationInfo) {
            setElement(DOM.createDiv());

            setStyles();

            m_icon.setUrl(locationInfo.getMarkerImageURL());
            m_nameLabel.setText(locationInfo.getName());
            m_areaLabel.setText(locationInfo.getArea());
            m_statusLabel.setText(locationInfo.getStatusDetails().getReason());
        }

        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            m_icon.addStyleName(locationDetailStyle.iconStyle());
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_areaLabel.addStyleName(locationDetailStyle.areaStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
            
        }
        
    }

    @Override
    protected Widget getListItemWidget(int rowIndex) {
        return new LocationInfoDisplay(getLocations().get(rowIndex));
    }

    /**
     * TODO: Maybe enhance this so that it only adds/updates/deletes individual items
     * TODO: Don't skip to the front page on every update
     */
    public void updateList(ArrayList<? extends LocationInfo> locations) {
        setLocations(locations);
        super.showFirstPage();
    }
    
    private ArrayList<? extends LocationInfo> getLocations() {
        return m_locations;
    }

    private void setLocations(ArrayList<? extends LocationInfo> locations) {
        m_locations = locations;
    }

    @Override
    protected int getListSize() {
        return getLocations().size();
    }

    @Override
    public void onItemClickHandler(ClickEvent event) {
      final Cell cell = getCellForEvent(event);
      LocationInfo location = m_locations.get(cell.getRowIndex() + (getCurrentPageIndex() * getTotalListItemsPerPage()));

      fireEvent(new LocationPanelSelectEvent(location.getName()));
    }

    public void refreshLocationListResize() {
        for(int i = 0; i < getDataList().getRowCount(); i++) {
            LocationInfoDisplay locInfo = (LocationInfoDisplay) getDataList().getWidget(i, 0);
            locInfo.resizeToFit();
        }
    }

}

package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;


public class PageableLocationList extends PageableList {
    
    private List<Location> m_locations;

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

        public LocationInfoDisplay(LocationInfo locationInfo) {
            setElement(Document.get().createDivElement());
            
            setStyles();
            
            m_icon.setUrl(locationInfo.getImageURL());
            m_nameLabel.setText(locationInfo.getName());
            m_areaLabel.setText(locationInfo.getArea());
            m_statusLabel.setText(locationInfo.getStatus().getReason());
            
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
        return new LocationInfoDisplay(getLocations().get(rowIndex).getLocationInfo());
    }
    
    public void updateList(List<Location> locations) {
        setLocations(locations);
        super.showFirstPage();
    }
    
    private List<Location> getLocations() {
        return m_locations;
    }
    
    private void setLocations(List<Location> locations) {
        m_locations = locations;
    }

    @Override
    protected int getListSize() {
        return getLocations().size();
    }

    @Override
    public void onItemClickHandler(ClickEvent event) {
      final Cell cell = getCellForEvent(event);
      Location location = m_locations.get(cell.getRowIndex() + (getCurrentPageIndex() * getTotalListItemsPerPage()));
      
      fireEvent(new LocationPanelSelectEvent(location.getLocationInfo().getName()));
        
    }

}

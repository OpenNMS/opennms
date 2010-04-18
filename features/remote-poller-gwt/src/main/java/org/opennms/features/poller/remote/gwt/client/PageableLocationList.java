package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PageableLocationList extends Composite {

    private static PageableLocationListUiBinder uiBinder = GWT.create(PageableLocationListUiBinder.class);

    interface PageableLocationListUiBinder extends UiBinder<Widget, PageableLocationList> {}
    
    interface LocationDetailStyle extends CssResource{
        String detailContainerStyle();
        String iconStyle();
        String nameStyle();
        String areaStyle();
        String statusStyle();
        String upStatus();
        String downStatus();
        String marginalStatus();
        String unknownStatus();
    }
    
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
            
            setStyles(locationInfo);
            
            m_icon.setUrl(locationInfo.getImageURL());
            m_nameLabel.setText(locationInfo.getName());
            m_areaLabel.setText(locationInfo.getArea());
            m_statusLabel.setText("This is the status text that will display");
            
        }

        private void setStyles(LocationInfo locationInfo) {
            setStyleName(locationDetailStyle.detailContainerStyle());
            addStyleName(getStatusStyle(locationInfo));
            m_icon.addStyleName(locationDetailStyle.iconStyle());
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_areaLabel.addStyleName(locationDetailStyle.areaStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
            
        }

        private String getStatusStyle(LocationInfo locationInfo) {
            switch(locationInfo.getApplicationStatus()) {
                case UP:
                    return locationDetailStyle.upStatus();
                
                case MARGINAL:
                    return locationDetailStyle.marginalStatus();
                
                case DOWN:
                    return locationDetailStyle.downStatus();
                    
                case UNKNOWN:
                    return locationDetailStyle.unknownStatus();
                    
                default:
                    return locationDetailStyle.unknownStatus();
            }
        }
        
    }
    
    private static final int TOTAL_LOCATIONS = 20;
    
    @UiField FlexTable dataList;
    @UiField FlowPanel pagingControls;
    @UiField Button nextBtn;
    @UiField Label pageStatsLabel;
    @UiField Button prevBtn;
    @UiField LocationDetailStyle locationDetailStyle;
    
    private int m_currentPageIndex = 0;
    private int m_totalPages = 0;
    private List<Location> m_locations;
    
    public PageableLocationList() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void updateList(List<Location> locations) {
        setLocations(locations);
        
        setCurrentPageIndex(0);
    }

    private void updateListDisplay(int currentPageIndex) {
        dataList.removeAllRows();
        
        int rowCount = 0;
        int showableLocations = ((currentPageIndex + 1) * TOTAL_LOCATIONS) > getLocations().size() ? getLocations().size() : (currentPageIndex * TOTAL_LOCATIONS);
        for(int i = currentPageIndex; i < showableLocations; i++) {
            
            dataList.setWidget(rowCount, 0, new LocationInfoDisplay(getLocations().get(i).getLocationInfo()));
            rowCount++;
        }
        
        setTotalPages( (int) Math.ceil(getLocations().size() / TOTAL_LOCATIONS) );
    }
    
    @UiHandler("prevBtn")
    public void onPrevBtnClick(ClickEvent event) {
        setCurrentPageIndex(getCurrentPageIndex() - 1); 
    }
    
    @UiHandler("nextBtn")
    public void onNextBtnClick(ClickEvent event) {
        setCurrentPageIndex(getCurrentPageIndex() + 1);
    }

    private void setCurrentPageIndex(int currentPageIndex) {
        if(currentPageIndex >= 0 && currentPageIndex <= getTotalPages()) {
            m_currentPageIndex = currentPageIndex;
            updateListDisplay(m_currentPageIndex);
        }
    }

    private int getCurrentPageIndex() {
        return m_currentPageIndex;
    }

    private void setTotalPages(int totalPages) {
        m_totalPages = totalPages;
        updatePageStatsDisplay();
    }

    private void updatePageStatsDisplay() {
        pageStatsLabel.setText(getCurrentPageIndex() + " of " + getTotalPages());
    }

    private int getTotalPages() {
        return m_totalPages;
    }

    public void setLocations(List<Location> locations) {
        m_locations = locations;
    }

    public List<Location> getLocations() {
        return m_locations;
    }

}

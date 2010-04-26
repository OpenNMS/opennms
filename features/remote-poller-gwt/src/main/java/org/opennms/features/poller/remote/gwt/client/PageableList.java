package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

/**
 * The Basic building blocks for a simple Pageable List
 * @author Donald Desloge
 *
 */
public abstract class PageableList extends Composite {

    private static PageableListUiBinder uiBinder = GWT.create(PageableListUiBinder.class);

    interface PageableListUiBinder extends UiBinder<Widget, PageableList> {}
    
    interface LocationDetailStyle extends CssResource{
        String detailContainerStyle();
        String iconStyle();
        String nameStyle();
        String areaStyle();
        String statusStyle();
        String alternateRowStyle();
    }
    
    private static final int TOTAL_LOCATIONS = 10;
    
    @UiField FlexTable dataList;
    @UiField Panel pagingControls;
    @UiField Hyperlink nextBtn;
    @UiField Label pageStatsLabel;
    @UiField Hyperlink prevBtn;
    @UiField LocationDetailStyle locationDetailStyle;
    
    private int m_currentPageIndex = 0;
    private int m_totalPages = 0;

    public PageableList() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    protected void showFirstPage() {
        setCurrentPageIndex(0);
    }

    protected void updateListDisplay(int currentPageIndex) {
        getDataList().removeAllRows();

        int rowCount = 0;
        int size = getListSize();
        int showableLocations = ((currentPageIndex + 1) * getTotalListItemsPerPage()) > size ? size : ((currentPageIndex + 1) * getTotalListItemsPerPage());
        int startIndex = currentPageIndex * getTotalListItemsPerPage(); 

        for(int i = startIndex; i < showableLocations; i++) {
            getDataList().setWidget(rowCount, 0, getListItemWidget(i));
            
            if(rowCount % 2 == 0) {
                getDataList().getRowFormatter().addStyleName(rowCount, getAlternateRowStyle());
            }
            
            rowCount++;
        }
        
        int totalPages = (int) Math.ceil(size / getTotalListItemsPerPage());
        if(totalPages == 0) {
            totalPages = 1;
        }
        setTotalPages( totalPages );
        updatePageStatsDisplay(startIndex + 1, showableLocations, getListSize());
    }

    protected String getAlternateRowStyle() {
        return locationDetailStyle.alternateRowStyle();
    }
    
    protected abstract int getListSize();
    
    protected abstract Widget getListItemWidget(int rowIndex);

    @UiHandler("dataList")
    public abstract void onItemClickHandler(final ClickEvent event);
    
    @UiHandler("prevBtn")
    public void onPrevBtnClick(ClickEvent event) {
        setCurrentPageIndex(getCurrentPageIndex() - 1); 
    }
    
    @UiHandler("nextBtn")
    public void onNextBtnClick(ClickEvent event) {
        setCurrentPageIndex(getCurrentPageIndex() + 1);
    }
    
    public void addLocationPanelSelectEventHandler(LocationPanelSelectEventHandler handler) {
        addHandler(handler, LocationPanelSelectEvent.TYPE);
    }
    
    private void setCurrentPageIndex(int currentPageIndex) {
        if(currentPageIndex == 0 ) {
            m_currentPageIndex = currentPageIndex;
            updateListDisplay(m_currentPageIndex);
        }else if(currentPageIndex > 0 && currentPageIndex < getTotalPages()) {
            m_currentPageIndex = currentPageIndex;
            updateListDisplay(m_currentPageIndex);
        }
    }

    protected int getCurrentPageIndex() {
        return m_currentPageIndex;
    }

    private void setTotalPages(int totalPages) {
        m_totalPages = totalPages;
    }

    private void updatePageStatsDisplay(int startIndex, int endIndex, int total) {
        if (endIndex > 0) {
            pageStatsLabel.setText( startIndex + "-" + endIndex + " of " + total);
        } else {
            pageStatsLabel.setText("No matching items");
        }
    }

    private int getTotalPages() {
        return m_totalPages;
    }

    protected int getTotalListItemsPerPage() {
        return TOTAL_LOCATIONS;
    }

    void setDataList(FlexTable dataList) {
        this.dataList = dataList;
    }

    FlexTable getDataList() {
        return dataList;
    }

    protected Cell getCellForEvent(ClickEvent event) {
        return getDataList().getCellForEvent(event);
    }

}

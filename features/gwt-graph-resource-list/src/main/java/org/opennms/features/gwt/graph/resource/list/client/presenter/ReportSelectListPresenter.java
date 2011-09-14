package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.presenter.DefaultResourceListPresenter.SearchPopupDisplay;
import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListPresenter implements Presenter, ReportSelectListView.Presenter<ResourceListItem> {

    private ReportSelectListView<ResourceListItem> m_view;
    private SearchPopupDisplay m_searchPopup;
    private String m_baseUrl;

    public ReportSelectListPresenter(ReportSelectListView<ResourceListItem> view, SearchPopupDisplay searchView, String baseUrl) {
        setView(view);
        getView().setPresenter(this);
        initializeSearchPopup(searchView);
        m_baseUrl = baseUrl;
    }
    
    
    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setHeightOffset(425);
        m_searchPopup.setTargetWidget(getView().searchPopupTarget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                getView().setDataList(filterList(m_searchPopup.getSearchText(), getView().getDataList()));
            }
        });
        
        m_searchPopup.getCancelBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
            }
        });
        
        m_searchPopup.getTextBox().addKeyPressHandler(new KeyPressHandler() {
            
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(event.getCharCode() == KeyCodes.KEY_ENTER) {
                    m_searchPopup.hideSearchPopup();
                    getView().setDataList(filterList(m_searchPopup.getSearchText(), getView().getDataList()));
                }
            }
        });
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(getView().asWidget());
    }


    @Override
    public void onGraphButtonClick() {
        List<ResourceListItem> reports = getView().getSelectedReports();
        if(reports != null) {
            buildUrlAndGoToGraphPage(reports);
        } else {
            getView().showWarning();
        }
        
        
    }


    private void buildUrlAndGoToGraphPage(List<ResourceListItem> reports) {
        StringBuilder sb = new StringBuilder();
        sb.append(m_baseUrl + "graph/results.htm?reports=all&resourceId=");
        
        
        boolean first = true;
        for(ResourceListItem item : reports) {
            if(!first) {
                
                sb.append("&resourceId=");
            }
            sb.append(item.getId());
            first = false;
        }
        
        Location.assign(sb.toString());
    }


    @Override
    public void onClearSelectionButtonClick() {
        getView().clearAllSelections();
        
    }


    @Override
    public void onSearchButtonClick() {
        m_searchPopup.showSearchPopup();
    }
    
    private List<ResourceListItem> filterList(String searchText, List<ResourceListItem> dataList) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : dataList) {
            if(item.getValue().contains(searchText)) {
                list.add(item);
            }
        }
        return list;
    }


    public void setView(ReportSelectListView<ResourceListItem> view) {
        m_view = view;
    }


    public ReportSelectListView<ResourceListItem> getView() {
        return m_view;
    }


    @Override
    public void onGraphAllButtonClick() {
        List<ResourceListItem> reports = getView().getAllReports();
        buildUrlAndGoToGraphPage(reports);
    }

}

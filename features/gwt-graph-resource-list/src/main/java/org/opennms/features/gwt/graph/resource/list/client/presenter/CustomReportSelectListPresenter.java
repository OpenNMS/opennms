package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.presenter.DefaultResourceListPresenter.SearchPopupDisplay;
import org.opennms.features.gwt.graph.resource.list.client.view.CustomReportSelectListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class CustomReportSelectListPresenter implements Presenter, CustomReportSelectListView.Presenter<ResourceListItem> {
    
    private CustomReportSelectListView<ResourceListItem> m_view;
    private SearchPopupDisplay m_searchPopup;
    private String m_baseUrl;
    private String m_endUrl;
    
    public CustomReportSelectListPresenter(CustomReportSelectListView<ResourceListItem> view, SearchPopupDisplay searchView, String baseUrl, String endUrl) {
        m_view = view;
        m_view.setPresenter(this);
        initializeSearchPopup(searchView);
        m_baseUrl = baseUrl;
        m_endUrl = endUrl;
    }

    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setHeightOffset(425);
        m_searchPopup.setTargetWidget(m_view.searchPopupTarget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                m_view.setDataList(filterList(m_searchPopup.getSearchText(), m_view.getDataList()));
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
                    m_view.setDataList(filterList(m_searchPopup.getSearchText(), m_view.getDataList()));
                }
            }
        });
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }

    @Override
    public void onSubmitButtonClick() {
        ResourceListItem report = m_view.getSelectedReport();
        if(report != null) {
            buildUrlAndGoToGraphPage(report);
        } else {
            m_view.showWarning();
        }
    }

    private void buildUrlAndGoToGraphPage(ResourceListItem report) {
        StringBuilder sb = new StringBuilder();
        sb.append(m_baseUrl).append(m_endUrl).append("?resourceId=").append(report.getId());
        Location.assign(sb.toString());
    }
    
    @Override
    public void onClearSelectionButtonClick() {
        m_view.clearAllSelections();
        
    }

    @Override
    public void onSearchButtonClick() {
        m_searchPopup.showSearchPopup();
    }
    
    private List<ResourceListItem> filterList(String searchText, List<ResourceListItem> dataList) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : dataList) {
            if(item.getValue().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(item);
            }
        }
        return list;
    }
}

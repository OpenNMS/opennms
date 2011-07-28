package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class DefaultResourceListPresenter implements Presenter, DefaultResourceListView.Presenter<ResourceListItem> {
    
    public interface SearchPopupDisplay {
        HasClickHandlers getSearchConfirmBtn();
        HasClickHandlers getCancelBtn();
        HasKeyPressHandlers getTextBox();
        Widget asWidget();
        String getSearchText();
        void setHeightOffset(int offset);
        void showSearchPopup();
        void hideSearchPopup();
        void setTargetWidget(Widget target);
    }
    
    private DefaultResourceListView<ResourceListItem> m_view;
    private SearchPopupDisplay m_searchPopup;
    private List<ResourceListItem> m_dataList;

    public DefaultResourceListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList) {
        setView(view);
        getView().setPresenter(this);
        
        initializeSearchPopup(searchPopup);
        
        m_dataList = convertJsArrayToList(dataList);
        getView().setDataList(m_dataList);
    }
    
    private List<ResourceListItem> convertJsArrayToList(JsArray<ResourceListItem> resourceList) {
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        for(int i = 0; i < resourceList.length(); i++) {
            data.add(resourceList.get(i));
        }
        return data;
    }
    
    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setTargetWidget(getView().asWidget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                getView().setDataList(filterList(m_searchPopup.getSearchText()));
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
                    getView().setDataList(filterList(m_searchPopup.getSearchText()));
                }
            }
        });
    }
    
    private List<ResourceListItem> filterList(String searchText) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : m_dataList) {
            if(item.getValue().contains(searchText)) {
                list.add(item);
            }
        }
        return list;
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(getView().asWidget());
    }

    @Override
    public void onResourceItemSelected() {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setHost(Location.getHost());
        urlBuilder.setPath("opennms/graph/chooseresource.htm");
        urlBuilder.setParameter("reports", "all");
        urlBuilder.setParameter("parentResourceId", getView().getSelectedResource().getId());
        
        Location.assign(urlBuilder.buildString());
    }

    @Override
    public void onSearchButtonClicked() {
        m_searchPopup.showSearchPopup();
    }

    public void setView(DefaultResourceListView<ResourceListItem> view) {
        m_view = view;
    }

    public DefaultResourceListView<ResourceListItem> getView() {
        return m_view;
    }

    

}

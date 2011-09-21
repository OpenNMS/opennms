package org.opennms.features.gwt.graph.resource.list.client.presenter;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.KscCustomSelectionView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class KscCustomReportListPresenter extends DefaultResourceListPresenter implements Presenter {
    

    public interface SelectionDisplay{
        HasClickHandlers getSubmitButton();
        String getSelectAction();
        Widget asWidget();
    }

    private SelectionDisplay m_selectionDisplay;
    
    public KscCustomReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList, SelectionDisplay selectionDisplay, String baseUrl) {
        super(view, searchPopup, dataList, baseUrl);
        initializeSelectionDisplay(selectionDisplay);
    }

    private void initializeSelectionDisplay(SelectionDisplay selectionDisplay) {
        m_selectionDisplay = selectionDisplay;
        
        m_selectionDisplay.getSubmitButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(getBaseUrl() + "/KSC/formProcMain.htm");
                
                if(m_selectionDisplay.getSelectAction() != null) {
                    if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.VIEW)) {
                        urlBuilder.append("?report_action=View");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CUSTOMIZE)) {
                        urlBuilder.append("?report_action=Customize");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW)) {
                        urlBuilder.append("?report_action=Create");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW_FROM_EXISTING)) {
                        urlBuilder.append("?report_action=CreateFrom");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.DELETE)) {
                        urlBuilder.append("?report_action=Delete");
                    }
                    
                    if(getView().getSelectedResource() != null) {
                        urlBuilder.append("&report=" +  getView().getSelectedResource().getId());
                        Location.assign(urlBuilder.toString());
                    } else if(getView().getSelectedResource() == null && m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW)) {
                        Location.assign(urlBuilder.toString());
                    }else {
                        getView().showWarning();
                    }
                } else {
                    getView().showWarning();
                }
                
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        super.go(container);
        container.add(m_selectionDisplay.asWidget());
        
    }
    
    @Override
    public void onResourceItemSelected() {
        //Don't do anything on selection
    }

}

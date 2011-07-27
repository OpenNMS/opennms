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
    
    public KscCustomReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList, SelectionDisplay selectionDisplay) {
        super(view, searchPopup, dataList);
        initializeSelectionDisplay(selectionDisplay);
    }

    private void initializeSelectionDisplay(SelectionDisplay selectionDisplay) {
        m_selectionDisplay = selectionDisplay;
        
        m_selectionDisplay.getSubmitButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String url = null;
                if(m_selectionDisplay.getSelectAction() != null) {
                    if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.VIEW)) {
                        url = "KSC/formProcMain.htm?report_action=View&report="+ getView().getSelectedResource().getId();
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CUSTOMIZE)) {
                        url = "KSC/formProcMain.htm?report_action=Customize&report="+ getView().getSelectedResource().getId();
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW)) {
                        url = "KSC/formProcMain.htm?report_action=Create&report="+ getView().getSelectedResource().getId();
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW_FROM_EXISTING)) {
                        url = "KSC/formProcMain.htm?report_action=CreateFrom&report="+ getView().getSelectedResource().getId();
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.DELETE)) {
                        url = "KSC/formProcMain.htm?report_action=Delete&report="+ getView().getSelectedResource().getId();
                    }
                }
                
                if(url != null) {
                    Location.assign(url);
                }else {
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

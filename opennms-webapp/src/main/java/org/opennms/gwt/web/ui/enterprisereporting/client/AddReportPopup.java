package org.opennms.gwt.web.ui.enterprisereporting.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AddReportPopup extends Composite {

    private static AddReportPopupUiBinder uiBinder = GWT.create(AddReportPopupUiBinder.class);

    interface AddReportPopupUiBinder extends UiBinder<Widget, AddReportPopup> {}
    
    @UiField
    public TextBox reportNameTF;
    
    @UiField
    public TextBox reportTemplateTF;
    
    @UiField
    public ListBox reportFormat;
    
    @UiField
    public TextBox cronScheduleTF;
    
    @UiField
    public Button addBtn;
    
    @UiField
    public Button cancelBtn;
    
    private PopupPanel m_popupPanelRef;
    
    public AddReportPopup(PopupPanel panel) {
        initWidget(uiBinder.createAndBindUi(this));
        m_popupPanelRef = panel;
    }
    
    
    @UiHandler("addBtn")
    public void handleAddReport(ClickEvent e) {
        
    }
    
    @UiHandler("cancelBtn")
    public void handleCancelReport(ClickEvent e) {
        m_popupPanelRef.hide();
    }

}

package org.opennms.features.gwt.graph.resource.list.client.view;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscCustomReportListPresenter.SelectionDisplay;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KscCustomSelectionView implements SelectionDisplay {
    
    public static final String VIEW = "view";
    public static final String CUSTOMIZE = "customize";
    public static final String CREATE_NEW = "createNew";
    public static final String CREATE_NEW_FROM_EXISTING = "createNewExisting";
    public static final String DELETE = "delete";
    VerticalPanel m_vertPanel;
    Button m_submitButton;
    RadioButton m_viewRB;
    RadioButton m_customizeRB;
    RadioButton m_createNewRB;
    RadioButton m_createNewExistingRB;
    RadioButton m_deleteRB;
    
    public KscCustomSelectionView() {
        m_vertPanel = new VerticalPanel();
        m_submitButton = new Button("Submit");
        m_viewRB = new RadioButton("group1", "View");
        m_customizeRB = new RadioButton("group1","Customize");
        m_createNewRB = new RadioButton("group1","Create New");
        m_createNewExistingRB = new RadioButton("group1","Create New from Existing");
        m_deleteRB = new RadioButton("group1","Delete");
        
        m_vertPanel.add(m_viewRB);
        m_vertPanel.add(m_customizeRB);
        m_vertPanel.add(m_createNewRB);
        m_vertPanel.add(m_createNewExistingRB);
        m_vertPanel.add(m_deleteRB);
        m_vertPanel.add(m_submitButton);
        
    }
    
    @Override
    public HasClickHandlers getSubmitButton() {
        return m_submitButton;
    }

    @Override
    public String getSelectAction() {
        if(m_viewRB.getValue()) {
            return VIEW;
        }else if(m_customizeRB.getValue()) {
            return CUSTOMIZE;
        }else if(m_createNewRB.getValue()) {
            return CREATE_NEW;
        }else if(m_createNewExistingRB.getValue()) {
            return CREATE_NEW_FROM_EXISTING;
        }else if(m_deleteRB.getValue()) {
            return DELETE;
        }
        return null;
    }

    @Override
    public Widget asWidget() {
        return m_vertPanel.asWidget();
    }

}

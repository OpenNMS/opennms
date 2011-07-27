package org.opennms.features.gwt.graph.resource.list.client.view;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscGraphResourceListPresenter.ViewChoiceDisplay;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KscReportResourceChooser implements ViewChoiceDisplay {
    
    VerticalPanel m_vertPanel;
    private Button m_chooseBtn;
    private Button m_viewBtn;
    
    public KscReportResourceChooser() {
        m_chooseBtn = new Button("Choose Child Resource");
        m_viewBtn = new Button("View Child Resource");
        
        m_vertPanel = new VerticalPanel();
        m_vertPanel.setStyleName("onms-table-no-borders-margin");
        m_vertPanel.add(m_chooseBtn);
        m_vertPanel.add(m_viewBtn);
    }
    
    @Override
    public HasClickHandlers getViewButton() {
        return m_viewBtn;
    }

    @Override
    public HasClickHandlers getChooseButton() {
        return m_chooseBtn;
    }

    @Override
    public Widget asWidget() {
        return m_vertPanel.asWidget();
    }

}

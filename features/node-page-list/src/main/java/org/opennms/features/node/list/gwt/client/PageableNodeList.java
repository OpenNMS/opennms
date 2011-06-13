package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class PageableNodeList extends Composite {

    private static PageableNodeListUiBinder uiBinder = GWT.create(PageableNodeListUiBinder.class);

    interface PageableNodeListUiBinder extends UiBinder<Widget, PageableNodeList> {}
    
    @UiField
    TabPanel m_tabPanel;
    
    public PageableNodeList() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}

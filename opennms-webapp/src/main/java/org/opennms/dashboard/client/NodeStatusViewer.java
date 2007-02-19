package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class NodeStatusViewer extends Composite {

    VerticalPanel m_panel = new VerticalPanel();
    ListBox m_listBox = new ListBox();
    
    public NodeStatusViewer() {
        Label label = new Label("nodeStatus");
        
        m_panel.add(label);
        m_panel.add(m_listBox);
        
        initWidget(m_panel);
    }

    public void setNodes(String[] nodeNames) {
        for(int i = 0; i < nodeNames.length; i++) {
            m_listBox.addItem(nodeNames[i]);
        }
    }

}

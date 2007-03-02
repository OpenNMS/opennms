package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class NodeStatusDashlet extends Dashlet {
    
    ListBox m_listBox = new ListBox();
    
    public NodeStatusDashlet(Dashboard dashboard) {
        super(dashboard, "Node Status");
        
        setView(m_listBox);
    }

    public void setNodes(String[] nodeNames) {
        for(int i = 0; i < nodeNames.length; i++) {
            m_listBox.addItem(nodeNames[i]);
        }
    }

}

package org.opennms.features.vaadin.topology;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

public class TopologyWidgetTestApplication extends Application {

    private Window m_window;
    private TopologyComponent m_topologyComponent;
    
    @Override
    public void init() {
        m_window = new Window("Topology Widget Test");
        setMainWindow(m_window);
        m_topologyComponent = new TopologyComponent();
        m_window.addComponent(m_topologyComponent);
        
        Button myButton = new Button();
        myButton.addListener(new ClickListener(){

            public void buttonClick(ClickEvent event) {
                m_topologyComponent.setDataArray(new Integer[] {1,2,3});
            }
        });
        
        m_window.addComponent(myButton);
    }

}

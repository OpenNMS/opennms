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
        
        Button addButton = new Button("Add");
        addButton.addListener(new ClickListener(){

            public void buttonClick(ClickEvent event) {
            	int sz = m_topologyComponent.getDataArray().length;
            	
            	Integer[] dataArray = new Integer[sz+1];
            	for(int i = 0; i < dataArray.length; i++) {
            		dataArray[i] = (i+1)*10;
            	}
                m_topologyComponent.setDataArray(dataArray);
            }
        });
        
        Button resetButton = new Button("Reset");
        resetButton.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				m_topologyComponent.setDataArray(new Integer[] { 10 });
			}
        	
        });
        
        m_window.addComponent(addButton);
        m_window.addComponent(resetButton);
    }

}

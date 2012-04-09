package org.opennms.features.vaadin.topology;


import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

public class TopologyWidgetTestApplication extends Application implements Action.Handler {

    private Window m_window;
    private TopologyComponent m_topologyComponent;
	private Action[] m_actions = new Action[] {new Action("Group")};
    
    @Override
    public void init() {
        m_window = new Window("Topology Widget Test");
        setMainWindow(m_window);
        m_topologyComponent = new TopologyComponent();
        m_topologyComponent.addActionHandler(this);
        m_window.addComponent(m_topologyComponent);
        
        final Slider slider = new Slider(0, 3);
        slider.setResolution(2);
        slider.setWidth("300px");
        slider.setOrientation(Slider.ORIENTATION_HORIZONTAL);
        slider.addListener(new ValueChangeListener(){

			public void valueChange(ValueChangeEvent event) {
				double scale = (Double) slider.getValue();
				
				m_topologyComponent.setScale(scale);
			}
		});
        slider.setImmediate(true);
        try {
			slider.setValue(m_topologyComponent.getScale());
		} catch (ValueOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        Button addButton = new Button("Add a Vertex");
        addButton.setDescription("Use this button to add a new Vertex");
        addButton.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				m_topologyComponent.addRandomNode();
			}
        });
        DragAndDropWrapper dndWrapper = new DragAndDropWrapper(addButton);
        dndWrapper.setDragStartMode(DragStartMode.WRAPPER);
        dndWrapper.setSizeUndefined();
        
        Button resetButton = new Button("Reset");
        resetButton.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                m_topologyComponent.resetGraph();
            }
        });
        
        Button removeVertexButton = new Button("Remove Vertex");
        removeVertexButton.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                m_topologyComponent.removeVertex();
            }
            
        });
        m_window.addComponent(slider);
        m_window.addComponent(dndWrapper);
        //m_window.addComponent(addButton);
        m_window.addComponent(resetButton);
        m_window.addComponent(removeVertexButton);
        Tree tree = new Tree();
    }

	public Action[] getActions(Object target, Object sender) {
		return m_actions;
	}

	public void handleAction(Action action, Object sender, Object target) {
		if(action.getCaption().equals("Group")) {
			//Do Something here
		}
		
	}

}

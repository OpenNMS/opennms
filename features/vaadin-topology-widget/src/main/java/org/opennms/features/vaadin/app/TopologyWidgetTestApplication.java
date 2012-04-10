package org.opennms.features.vaadin.app;



import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.Vertex;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TopologyWidgetTestApplication extends Application implements Action.Handler {

    private Window m_window;
    private TopologyComponent m_topologyComponent;
    
    private Action[] m_mapAction = new Action[] {
    		new Action("Redo Layout")
    };
    
    private Action[] m_vertexActions = new Action[] {
    		new Action("Group"),
			new Action("Vertex Action"),
    };
    
    private Action[] m_vertexZeroActions = new Action[] {
			new Action("Vertex 0 Action")
    };

    
    @Override
    public void init() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(layout);
        setMainWindow(m_window);
        
        m_topologyComponent = new TopologyComponent();
        m_topologyComponent.addActionHandler(this);
        m_topologyComponent.setSizeFull();
        
        
        
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
			e.printStackTrace();
		}
        
        
        Button addButton = new Button("Add a Vertex");
        addButton.setDescription("Use this button to add a new Vertex");
        addButton.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				m_topologyComponent.addRandomNode();
			}
        });
        
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
        
        HorizontalLayout topLayoutBar = new HorizontalLayout();
        topLayoutBar.addComponent(addButton);
        topLayoutBar.addComponent(resetButton);
        topLayoutBar.addComponent(removeVertexButton);
        topLayoutBar.addComponent(slider);
        
        HorizontalLayout bottomLayoutBar = new HorizontalLayout();
        bottomLayoutBar.addComponent(new Button("Bottom bar"));
        
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setWidth("100px");
        vLayout.setHeight("100%");
        vLayout.addComponent(new Button("Hello There"));
        
        HorizontalLayout mapLayout = new HorizontalLayout();
        
        mapLayout.addComponent(vLayout);
        mapLayout.addComponent(m_topologyComponent);
        mapLayout.setSizeFull();
        mapLayout.setExpandRatio(m_topologyComponent, 1.0f);
        
        
        
        layout.addComponent(topLayoutBar);
        layout.addComponent(mapLayout);
        layout.addComponent(bottomLayoutBar);
        layout.setExpandRatio(mapLayout, 1.0f);
        
        
    }

	private Component createTree() {
	    Tree tree = new Tree();
	    tree.setWidth("100%");
	    tree.setHeight("100%");
	    
	    Graph graph = m_topologyComponent.getGraph();
	    for(Vertex vertex : graph.getVertices()) {
	        tree.addItem(vertex);
	    }
	    
        return tree;
    }

    public Action[] getActions(Object target, Object sender) {
    	List<Action> applicableActions = new LinkedList<Action>();
    	
    	if (target == null) {
    		return m_mapAction;
    	} else if (target instanceof Vertex && ((Vertex)target).getId().equals("0")) {
    		applicableActions.addAll(Arrays.asList(m_vertexActions));
    		applicableActions.addAll(Arrays.asList(m_vertexZeroActions));
    		return applicableActions.toArray(new Action[0]);
    	} else if (target instanceof Vertex) {
    		return m_vertexActions;
    	} else {
    		return new Action[0];
    	}
    	
	}

	public void handleAction(Action action, Object sender, Object target) {
		System.err.println("Topology App: Got Action " + action.getCaption() + " for target " + target);
	}

}

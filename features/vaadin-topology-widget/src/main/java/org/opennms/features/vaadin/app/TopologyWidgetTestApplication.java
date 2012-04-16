package org.opennms.features.vaadin.app;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.Vertex;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
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
    
    private MenuBar m_menuBar = new MenuBar();

    
    @Override
    public void init() {
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(layout);
        setMainWindow(m_window);
        
        final MenuBar.MenuItem addVertex  = m_menuBar.addItem("Add Vertex", new MenuBar.Command() {
            
            public void menuSelected(MenuItem selectedItem) {
                m_topologyComponent.addRandomNode();
            }
        });
        
        final MenuBar.MenuItem removeVertex = m_menuBar.addItem("Remove Vertex", new MenuBar.Command() {
            
            public void menuSelected(MenuItem selectedItem) {
                m_topologyComponent.removeVertex();
            }
        });
        
        final MenuBar.MenuItem reset = m_menuBar.addItem("Reset", new MenuBar.Command() {
            
            public void menuSelected(MenuItem selectedItem) {
                m_topologyComponent.resetGraph();
            }
        });
        m_menuBar.setWidth("100%");
        
        m_topologyComponent = new TopologyComponent();
        m_topologyComponent.addActionHandler(this);
        m_topologyComponent.setSizeFull();
        
        
        
        final Slider slider = new Slider(0, 3);
        slider.setResolution(2);
        slider.setHeight("300px");
        slider.setOrientation(Slider.ORIENTATION_VERTICAL);
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
        
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setWidth("100px");
        vLayout.setHeight("100%");
        vLayout.addComponent(new Button("Hello There"));
        
        HorizontalLayout mapLayout = new HorizontalLayout();
        
        mapLayout.addComponent(vLayout);
        mapLayout.addComponent(m_topologyComponent);
        mapLayout.setSizeFull();
        mapLayout.setExpandRatio(m_topologyComponent, 1.0f);
        
        VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
        bottomLayoutBar.setFirstComponent(mapLayout);
        bottomLayoutBar.setSecondComponent(new Button("Bottom bar"));
        bottomLayoutBar.setSplitPosition(80, Sizeable.UNITS_PERCENTAGE);
        bottomLayoutBar.setSizeFull();
        
        layout.addComponent(m_menuBar, "top: 0px; left: 0px; right:0px;");
        layout.addComponent(bottomLayoutBar, "top: 23px; left: 0px; right:0px; bottom:0px;");
        layout.addComponent(slider, "top: 56px; left: 130px; z-index:1000;");
        
        
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

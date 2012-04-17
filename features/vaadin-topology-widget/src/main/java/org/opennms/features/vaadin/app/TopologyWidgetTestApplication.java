package org.opennms.features.vaadin.app;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.features.vaadin.topology.Edge;
import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.MenuBarBuilder;
import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.Vertex;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

public class TopologyWidgetTestApplication extends Application implements Action.Handler {

    private Window m_window;
    private TopologyComponent m_topologyComponent;
    
    private Command[] m_commands = new Command[] {new Command("Redo Layout") {

        @Override
        public void doCommand(Object target) {
            m_topologyComponent.redoLayout();
        }

        @Override
        public void undoCommand() {
            
        }

        @Override
        public boolean appliesToTarget(Object target) {
            //Applies to background as a whole
            return target == null;
        }
    }.setAction(),
    new Command("Add Vertex") {

        @Override
        public boolean appliesToTarget(Object target) {
            if(target instanceof Edge) {
                return false;
            }
            return true;
        }

        @Override
        public void doCommand(Object target) {
            if(target instanceof Vertex) {
                m_topologyComponent.addVertexTo((Vertex)target);
            }else {
                m_topologyComponent.addRandomNode();
            }
            updateTree();
        }

        @Override
        public void undoCommand() {
            // TODO Auto-generated method stub
            
        }
    }.setParentMenu("File").setAction(),
    new Command("Remove Vertex") {

        @Override
        public boolean appliesToTarget(Object target) {
            if(target instanceof Edge) {
                return false;
            }
            return true;
        }

        @Override
        public void doCommand(Object target) {
            if(target instanceof Vertex) {
                m_topologyComponent.removeVertex((Vertex)target);
            }else {
                m_topologyComponent.removeVertex();
            }
            
            updateTree();
        }

        @Override
        public void undoCommand() {
            // TODO Auto-generated method stub
            
        }
        
    }.setParentMenu("File").setAction(),
    new Command("Reset") {

        @Override
        public boolean appliesToTarget(Object target) {
            return true;
        }

        @Override
        public void doCommand(Object target) {
            m_topologyComponent.resetGraph();
            updateTree();
        }

        @Override
        public void undoCommand() {
            // TODO Auto-generated method stub
            
        }
    }.setParentMenu(null)
    
    };
    
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
    
    private Tree m_tree;

    
    @Override
    public void init() {
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(layout);
        setMainWindow(m_window);
        
        m_topologyComponent = new TopologyComponent();
        addActionHandlersToTopologyMap();
        //m_topologyComponent.addActionHandler(this);
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
        
        m_tree = createTree();
        
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setWidth("100%");
        vLayout.setHeight("100%");
        vLayout.addComponent(m_tree);
        
        AbsoluteLayout mapLayout = new AbsoluteLayout();
        
        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(slider, "top: 20px; left: 20px; z-index:1000;");
        mapLayout.setSizeFull();
        
        HorizontalSplitPanel treeMapSplitPanel = new HorizontalSplitPanel();
        treeMapSplitPanel.setFirstComponent(vLayout);
        treeMapSplitPanel.setSecondComponent(mapLayout);
        treeMapSplitPanel.setSplitPosition(100, Sizeable.UNITS_PIXELS);
        treeMapSplitPanel.setSizeFull();
        
        
        VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
        bottomLayoutBar.setFirstComponent(treeMapSplitPanel);
        bottomLayoutBar.setSecondComponent(new Button("Bottom bar"));
        bottomLayoutBar.setSplitPosition(80, Sizeable.UNITS_PERCENTAGE);
        bottomLayoutBar.setSizeFull();
        
        MenuBarBuilder menuBarBuilder = new MenuBarBuilder();
        menuBarBuilder.addCommands(m_commands);
        MenuBar menuBar = menuBarBuilder.get();
        menuBar.setWidth("100%");
        layout.addComponent(menuBar, "top: 0px; left: 0px; right:0px;");
        layout.addComponent(bottomLayoutBar, "top: 23px; left: 0px; right:0px; bottom:0px;");
        
    }
    
    protected void updateTree() {
        m_tree.removeAllItems();
        for(Vertex vert : m_topologyComponent.getGraph().getVertices()) {
            m_tree.addItem(vert);
        }
    }

    private Tree createTree() {
        Graph graph = m_topologyComponent.getGraph();
        
        Tree tree = new Tree("Vertices");
        
        for(Vertex vert : graph.getVertices()) {
            tree.addItem(vert);
        }
        tree.setImmediate(true);
        
        for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
            tree.expandItemsRecursively(it.next());
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
    
    public void addActionHandlersToTopologyMap() {
        for(Command command : m_commands) {
            m_topologyComponent.addActionHandler(command);
        }
    }

	public void handleAction(Action action, Object sender, Object target) {
		System.err.println("Topology App: Got Action " + action.getCaption() + " for target " + target);
	}

}

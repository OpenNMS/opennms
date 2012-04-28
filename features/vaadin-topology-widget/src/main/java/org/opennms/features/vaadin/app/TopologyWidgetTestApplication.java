package org.opennms.features.vaadin.app;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opennms.features.vaadin.topology.AlternativeLayoutAlgorithm;
import org.opennms.features.vaadin.topology.Edge;
import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.GraphContainer;
import org.opennms.features.vaadin.topology.SimpleGraphContainer;
import org.opennms.features.vaadin.topology.SimpleLayoutAlgorithm;
import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.Vertex;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;

public class TopologyWidgetTestApplication extends Application{
	
	private static final String CENTER_VERTEX_ID = "center";

    private Window m_window;
    private TopologyComponent m_topologyComponent;
    private CommandManager m_commandManager = new CommandManager();
    private Tree m_tree;
    private SimpleGraphContainer m_graphContainer = new SimpleGraphContainer();
    
    @Override
    public void init() {
        m_commandManager.addCommand(new Command("Redo Layout") {;

            @Override
            public void doCommand(Object target) {
                //TODO: Move this to the container
            	m_graphContainer.redoLayout();
            	
            }
    
            @Override
            public void undoCommand() {
                
            }
    
            @Override
            public boolean appliesToTarget(Object target) {
                //Applies to background as a whole
                return target == null;
            }
        }, true);
        
        m_commandManager.addCommand(new Command("Add Vertex") {

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
                	Vertex v = (Vertex) target;
                	
                	addVertexTo(v);
                    
                	//m_topologyComponent.addVertexTo((Vertex)target);
                }else {
                	addRandomVertex();
                	
                    //m_topologyComponent.addRandomNode();
                }
                
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
        }, true, "File");
        
        m_commandManager.addCommand(new Command("Add Switch Vertex") {

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
                	addVertexTo((Vertex) target);
                	
                    //m_topologyComponent.addSwitchVertexTo((Vertex)target);
                }else {
                    addRandomVertex();
                	//m_topologyComponent.addRandomNode();
                }
                
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
        }, true);
        
        m_commandManager.addCommand(new Command("Remove Vertex") {

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
                	Vertex v = (Vertex) target;
                	m_graphContainer.removeVertex(v.getKey());
                	
                    //m_topologyComponent.removeVertex((Vertex)target);
                }else {
                	//TODO: Do we even want to remove a random vertex??
                    //m_topologyComponent.removeVertex();
                }
                
                
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
            
        }, true, "File");
        
        m_commandManager.addCommand(new Command("Simple Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				//TODO: Move this to the Container
				m_graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
				//m_topologyComponent.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
			}

			@Override
			public void undoCommand() {
				throw new UnsupportedOperationException("Command.undoCommand is not yet implemented.");
			}
        	
        }, false, "Edit|Layout");
        m_commandManager.addCommand(new Command("Other Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				//TODO: Move this into the Container
				m_graphContainer.setLayoutAlgorithm(new AlternativeLayoutAlgorithm());
			}

			@Override
			public void undoCommand() {
				throw new UnsupportedOperationException("Command.undoCommand is not yet implemented.");
			}}, false, "Edit|Layout");
        
        m_commandManager.addCommand(new Command("Reset") {

            @Override
            public boolean appliesToTarget(Object target) {
                return true;
            }

            @Override
            public void doCommand(Object target) {
            	
                resetView();
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
        }, false, null);
        
        m_commandManager.addCommand(new Command("History") {

            @Override
            public boolean appliesToTarget(Object target) {
                return true;
            }

            @Override
            public void doCommand(Object target) {
                showHistoryList(m_commandManager.getHistoryList());
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }}, false, null);
        
        
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(layout);
        setMainWindow(m_window);
        
        m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50);
        
        
        m_topologyComponent = new TopologyComponent(m_graphContainer);
        m_commandManager.addActionHandlers(m_topologyComponent);
        m_topologyComponent.setSizeFull();
        
        final Slider slider = new Slider(1, 4);
        slider.setResolution(2);
        slider.setHeight("300px");
        slider.setOrientation(Slider.ORIENTATION_VERTICAL);
        slider.addListener(new ValueChangeListener(){

			public void valueChange(ValueChangeEvent event) {
				double scale = (Double) slider.getValue();
				
				m_graphContainer.setScale(scale);
			}
		});
        slider.setImmediate(true);
        
        m_topologyComponent.setScaleSlider(slider);
        
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
        
        MenuBar menuBar = m_commandManager.getMenuBar();
        menuBar.setWidth("100%");
        layout.addComponent(menuBar, "top: 0px; left: 0px; right:0px;");
        layout.addComponent(bottomLayoutBar, "top: 23px; left: 0px; right:0px; bottom:0px;");
        
    }

    protected void showHistoryList(List<Command> historyList) {
        
        Window window = new Window();
        window.setModal(true);
        
        for(Command command : historyList) {
            window.addComponent(new Label(command.toString()));
        }
        
        getMainWindow().addWindow(window);
        
    }

    private Tree createTree() {
        
        Tree tree = new Tree("Vertices");
        tree.setContainerDataSource(m_graphContainer.getVertexContainer());
        
        tree.setImmediate(true);
        
        for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
            tree.expandItemsRecursively(it.next());
        }
        return tree;
    }

	private void addVertexTo(Vertex v) {
		//Add Vertex
		String targetId = m_graphContainer.getNextVertexId();
		m_graphContainer.addVertex(targetId, 0, 0);
		
		//Connect the source and target with an edge
		String edgeId = m_graphContainer.getNextEdgeId();
		m_graphContainer.connectVertices(edgeId, v.getKey(), targetId);
	}

	private void addRandomVertex() {
		if (m_graphContainer.getVertexContainer().containsId(CENTER_VERTEX_ID)) {
			String vertexId = m_graphContainer.getNextVertexId();
			m_graphContainer.addVertex(vertexId, 0, 0);

			//Right now we are connecting all new vertices to v0
			m_graphContainer.connectVertices(m_graphContainer.getNextEdgeId(), CENTER_VERTEX_ID, vertexId);
		}
		else {
			m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50);
		}
	}

	private void resetView() {
		m_graphContainer.resetContainer();
	}

}

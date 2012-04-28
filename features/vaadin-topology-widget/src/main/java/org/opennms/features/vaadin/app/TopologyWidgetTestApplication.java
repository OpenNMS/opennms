package org.opennms.features.vaadin.app;

import java.util.Iterator;
import java.util.List;

import org.opennms.features.vaadin.topology.AlternativeLayoutAlgorithm;
import org.opennms.features.vaadin.topology.SimpleGraphContainer;
import org.opennms.features.vaadin.topology.SimpleLayoutAlgorithm;
import org.opennms.features.vaadin.topology.TopologyComponent;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
	
	public static final String ROOT_GROUP_ID = "Network";
	public static final String CENTER_VERTEX_ID = "center";
	public static final String GROUP_ICON = "VAADIN/widgetsets/org.opennms.features.vaadin.topology.gwt.TopologyWidget/topologywidget/images/group.png";
	public static final String SERVER_ICON = "VAADIN/widgetsets/org.opennms.features.vaadin.topology.gwt.TopologyWidget/topologywidget/images/server.png";
	public static final String SWITCH_ICON = "VAADIN/widgetsets/org.opennms.features.vaadin.topology.gwt.TopologyWidget/topologywidget/images/srx100.png";

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
            public boolean appliesToTarget(Object itemId) {
            	return itemId == null || m_graphContainer.getVertexContainer().containsId(itemId);
            }

            @Override
            public void doCommand(Object vertexId) {
            	if (vertexId == null) {
            		addRandomVertex();
            	} else {
            		connectNewVertexTo(vertexId.toString(), SERVER_ICON);
            	}
            	m_graphContainer.redoLayout();
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
        }, true, "File");
        
        m_commandManager.addCommand(new Command("Add Switch Vertex") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	return itemId == null || m_graphContainer.getVertexContainer().containsId(itemId);
            }

            @Override
            public void doCommand(Object vertexId) {
            	if (vertexId == null) {
            		addRandomVertex();
            	} else {
            		connectNewVertexTo(vertexId.toString(), SWITCH_ICON);
            	}
            	m_graphContainer.redoLayout();
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
        }, true);
        
        m_commandManager.addCommand(new Command("Remove Vertex") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	return itemId == null || m_graphContainer.getVertexContainer().containsId(itemId);
            }

            @Override
            public void doCommand(Object vertexId) {
            	if (vertexId == null) {
            		System.err.println("need to handle selection!!!");
            	} else {
            		m_graphContainer.removeVertex(vertexId.toString());
            		m_graphContainer.redoLayout();
            	}
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
            
        }, true, "File");
        
        m_commandManager.addCommand(new Command("Create Group") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	return m_graphContainer.getSelectedVertexIds().size() > 0;
            }

            @Override
            public void doCommand(Object vertexId) {
            	String groupId = m_graphContainer.getNextGroupId();
            	m_graphContainer.addGroup(groupId, GROUP_ICON);
            	m_graphContainer.getVertexContainer().setParent(groupId, ROOT_GROUP_ID);
            	
            	for(Object itemId : m_graphContainer.getSelectedVertexIds()) {
            		m_graphContainer.getVertexContainer().setParent(itemId, groupId);
            	}
            }

            @Override
            public void undoCommand() {
                // TODO Auto-generated method stub
                
            }
            
        }, true, "Edit");
        
m_commandManager.addCommand(new Command("Simple Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
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
        
        m_graphContainer.addGroup(ROOT_GROUP_ID, GROUP_ICON);
        m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
        m_graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
        
        
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

	private void addRandomVertex() {
		if (m_graphContainer.getVertexContainer().containsId(CENTER_VERTEX_ID)) {
			connectNewVertexTo(CENTER_VERTEX_ID, SERVER_ICON);
		}
		else {
			m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
			m_graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
		}
	}

	private void connectNewVertexTo(String existingVertexId, String icon) {
		String vertexId = m_graphContainer.getNextVertexId();
		m_graphContainer.addVertex(vertexId, 0, 0, icon);
		m_graphContainer.getVertexContainer().setParent(vertexId, ROOT_GROUP_ID);

		//Right now we are connecting all new vertices to v0
		m_graphContainer.connectVertices(m_graphContainer.getNextEdgeId(), existingVertexId, vertexId);
	}

	private void resetView() {
		m_graphContainer.resetContainer();
		m_graphContainer.addGroup(ROOT_GROUP_ID, GROUP_ICON);
		m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
		m_graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
	}

}

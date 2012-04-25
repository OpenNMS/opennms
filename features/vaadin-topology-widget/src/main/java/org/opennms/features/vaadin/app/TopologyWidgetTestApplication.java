package org.opennms.features.vaadin.app;

import java.util.Iterator;
import java.util.List;

import org.opennms.features.vaadin.topology.AlternativeLayoutAlgorithm;
import org.opennms.features.vaadin.topology.Edge;
import org.opennms.features.vaadin.topology.Graph;
import org.opennms.features.vaadin.topology.SimpleLayoutAlgorithm;
import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.Vertex;

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

    private Window m_window;
    private TopologyComponent m_topologyComponent;
    private CommandManager m_commandManager = new CommandManager();
    private Tree m_tree;
    
    @Override
    public void init() {
        m_commandManager.addCommand(new Command("Redo Layout") {;

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
                    m_topologyComponent.addSwitchVertexTo((Vertex)target);
                }else {
                    m_topologyComponent.addRandomNode();
                }
                updateTree();
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
            
        }, true, "File");
        
        m_commandManager.addCommand(new Command("Simple Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_topologyComponent.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
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
				m_topologyComponent.setLayoutAlgorithm(new AlternativeLayoutAlgorithm());
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
                m_topologyComponent.resetGraph();
                updateTree();
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
        
        m_topologyComponent = new TopologyComponent();
        m_commandManager.addActionHandlers(m_topologyComponent);
        //m_topologyComponent.addActionHandler(this);
        m_topologyComponent.setSizeFull();
        
        final Slider slider = new Slider(1, 4);
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

}

package org.opennms.features.vaadin.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.features.vaadin.topology.AlternativeLayoutAlgorithm;
import org.opennms.features.vaadin.topology.ManualLayoutAlgorithm;
import org.opennms.features.vaadin.topology.SimpleGraphContainer;
import org.opennms.features.vaadin.topology.SimpleLayoutAlgorithm;
import org.opennms.features.vaadin.topology.TopologyComponent;
import org.opennms.features.vaadin.topology.jung.BalloonLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.CircleLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.DAGLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.FRLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.ISOMLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.KKLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.RadialTreeLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.SpringLayoutAlgorithm;
import org.opennms.features.vaadin.topology.jung.TreeLayoutAlgorithm;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Slider;
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
    private Timer m_timer = new Timer();
    private MenuBar m_menuBar;
    
    @Override
    public void init() {
        //This timer is a hack at the moment to disable and enable menuItems
        m_timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                List<MenuItem> items = m_menuBar.getItems();
                for(MenuItem item : items) {
                    if(item.getText().equals("Device")) {
                        List<MenuItem> children = item.getChildren();
                        for(MenuItem child : children) {
                            if(m_graphContainer.getSelectedVertexIds().size() > 0) {
                                if(!child.isEnabled()) {
                                    child.setEnabled(true);
                                }
                            }else {
                                if(child.isEnabled()) {
                                    child.setEnabled(false);
                                }
                                
                            }
                        }
                        
                        
                    }
                }
            }
        }, 1000, 1000);
        
        m_commandManager.addCommand(new Command("Redo Layout") {;

            @Override
            public void doCommand(Object target) {
            	m_graphContainer.redoLayout();
            }
    
            @Override
            public boolean appliesToTarget(Object target) {
                //Applies to background as a whole
                return target == null;
            }
        }, true);
        
        m_commandManager.addCommand(new Command("Open") {

            @Override
            public void doCommand(Object target) {
                m_graphContainer.load("graph.xml");
            }

        }, false, "File");
        
        m_commandManager.addCommand(new Command("Save") {

            @Override
            public void doCommand(Object target) {
                m_graphContainer.save("graph.xml");
            }

        }, false, "File");
        
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

        }, true, "File");
        
        m_commandManager.addCommand(new Command("Lock") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	if (m_graphContainer.getVertexContainer().containsId(itemId)) {
            		Item v = m_graphContainer.getVertexContainer().getItem(itemId);
            		return !(Boolean)v.getItemProperty("locked").getValue();
            	}
            	return false;
            }

            @Override
            public void doCommand(Object vertexId) {
            	Item v = m_graphContainer.getVertexContainer().getItem(vertexId);
            	v.getItemProperty("locked").setValue(true);
            }

        }, true);
        
        m_commandManager.addCommand(new Command("Unlock") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	if (m_graphContainer.getVertexContainer().containsId(itemId)) {
            		Item v = m_graphContainer.getVertexContainer().getItem(itemId);
            		return (Boolean)v.getItemProperty("locked").getValue();
            	}
            	return false;
            }

            @Override
            public void doCommand(Object vertexId) {
            	Item v = m_graphContainer.getVertexContainer().getItem(vertexId);
            	v.getItemProperty("locked").setValue(false);
            }

        }, true);
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


        }, true, "File");
        
        m_commandManager.addCommand(new Command("Connect") {

            @Override
            public boolean appliesToTarget(Object itemId) {
            	return m_graphContainer.getSelectedVertexIds().size() == 2;
            }

            @Override
            public void doCommand(Object unused) {
            	List<Object> endPoints = new ArrayList<Object>(m_graphContainer.getSelectedVertexIds());
            	
            	m_graphContainer.connectVertices(m_graphContainer.getNextEdgeId(), (String)endPoints.get(0), (String)endPoints.get(1));
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

        }, true, "Edit");
        
        m_commandManager.addCommand(new Command("Manual Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new ManualLayoutAlgorithm());
			}

        }, false, "Edit|Layout");

        m_commandManager.addCommand(new Command("Balloon Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new BalloonLayoutAlgorithm(CENTER_VERTEX_ID));
			}

        }, false, "Edit|Layout|JUNG");
        
        m_commandManager.addCommand(new Command("Circle Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new CircleLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        m_commandManager.addCommand(new Command("DAG Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new DAGLayoutAlgorithm(CENTER_VERTEX_ID));
			}

        }, false, "Edit|Layout|JUNG");

        m_commandManager.addCommand(new Command("Radial Tree Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new RadialTreeLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");
        m_commandManager.addCommand(new Command("Tree Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new TreeLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        m_commandManager.addCommand(new Command("Simple Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new SimpleLayoutAlgorithm());
			}

        }, false, "Edit|Layout");

        m_commandManager.addCommand(new Command("Spring Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new SpringLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        
        m_commandManager.addCommand(new Command("KK Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new KKLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        m_commandManager.addCommand(new Command("ISOM Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new ISOMLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        m_commandManager.addCommand(new Command("FR Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new FRLayoutAlgorithm());
			}

        }, false, "Edit|Layout|JUNG");

        
        m_commandManager.addCommand(new Command("Other Layout") {

			@Override
			public boolean appliesToTarget(Object target) {
				return true;
			}

			@Override
			public void doCommand(Object target) {
				m_graphContainer.setLayoutAlgorithm(new AlternativeLayoutAlgorithm());
			}

		}, false, "Edit|Layout");
        
        m_commandManager.addCommand(new Command("Reset") {

            @Override
            public boolean appliesToTarget(Object target) {
                return true;
            }

            @Override
            public void doCommand(Object target) {
            	
                resetView();
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

        }, false, null);
        
        m_commandManager.addCommand(new Command("Show Map") {

            @Override
            public void doCommand(Object target) {
                getMainWindow().showNotification("This has not been implemented yet");
                
            }

        }, false, "View");
        
        m_commandManager.addCommand(new Command("Get Info") {

            @Override
            public boolean appliesToTarget(Object itemId) {
                return itemId == null || m_graphContainer.getEdgeContainer().containsId(itemId);
            }

            @Override
            public void doCommand(Object target) {
                getMainWindow().showNotification("This has not been implemented yet");
            }

        }, true, "Device");
        
        
        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(layout);
        setMainWindow(m_window);
        
        m_graphContainer.addGroup(ROOT_GROUP_ID, GROUP_ICON);
        m_graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
        m_graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
        m_graphContainer.setLayoutAlgorithm(new KKLayoutAlgorithm());
        
        
        m_topologyComponent = new TopologyComponent(m_graphContainer);
        m_commandManager.addActionHandlers(m_topologyComponent);
        m_topologyComponent.setSizeFull();
        
        final Property scale = m_graphContainer.getProperty("scale");
        final Slider slider = new Slider(1, 4);
        slider.setResolution(2);
        slider.setHeight("300px");
        slider.setOrientation(Slider.ORIENTATION_VERTICAL);
        
        slider.addListener(new ValueChangeListener(){

			public void valueChange(ValueChangeEvent event) {
				scale.setValue((Double) slider.getValue());
			}
		});
        
        slider.setImmediate(true);
        
        m_tree = createTree();
        Label semanticZoomLabel = new Label();
        final Property zoomLevel = m_graphContainer.getProperty("semanticZoomLevel");
		semanticZoomLabel.setPropertyDataSource(zoomLevel);
        
        Button zoomInBtn = new Button("Zoom In");
        zoomInBtn.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				int szl = (Integer) zoomLevel.getValue();
				szl++;
				zoomLevel.setValue(szl);
				m_graphContainer.redoLayout();
			}
		});
        
        Button zoomOutBtn = new Button("Zoom Out");
        zoomOutBtn.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				int szl = (Integer) zoomLevel.getValue();
				szl--;
				zoomLevel.setValue(szl);
				m_graphContainer.redoLayout();
			}
		});
        
        
        VerticalLayout vLayout = new VerticalLayout();
        vLayout.setWidth("100%");
        vLayout.setHeight("100%");
        vLayout.addComponent(m_tree);
        
        AbsoluteLayout mapLayout = new AbsoluteLayout();
        
        mapLayout.addComponent(m_topologyComponent, "top:0px; left: 0px; right: 0px; bottom: 0px;");
        mapLayout.addComponent(slider, "top: 20px; left: 20px; z-index:1000;");
        mapLayout.addComponent(semanticZoomLabel, "bottom: 10px; right: 10px; z-index: 2000;");
        mapLayout.setSizeFull();
        
        HorizontalSplitPanel treeMapSplitPanel = new HorizontalSplitPanel();
        treeMapSplitPanel.setFirstComponent(vLayout);
        treeMapSplitPanel.setSecondComponent(mapLayout);
        treeMapSplitPanel.setSplitPosition(100, Sizeable.UNITS_PIXELS);
        treeMapSplitPanel.setSizeFull();
        
        
        VerticalSplitPanel bottomLayoutBar = new VerticalSplitPanel();
        bottomLayoutBar.setFirstComponent(treeMapSplitPanel);
        
        VerticalLayout zoomLayout = new VerticalLayout();
        zoomLayout.addComponent(zoomInBtn);
        zoomLayout.addComponent(zoomOutBtn);
        
		bottomLayoutBar.setSecondComponent(zoomLayout);
        bottomLayoutBar.setSplitPosition(80, Sizeable.UNITS_PERCENTAGE);
        bottomLayoutBar.setSizeFull();
        
        m_menuBar = m_commandManager.getMenuBar();
        m_menuBar.setWidth("100%");
        layout.addComponent(m_menuBar, "top: 0px; left: 0px; right:0px;");
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

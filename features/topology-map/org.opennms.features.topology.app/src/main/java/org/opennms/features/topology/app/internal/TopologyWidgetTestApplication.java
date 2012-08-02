package org.opennms.features.topology.app.internal;

import java.util.Iterator;
import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.app.internal.jung.KKLayoutAlgorithm;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
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
import com.vaadin.ui.Window.CloseEvent;

public class TopologyWidgetTestApplication extends Application implements CommandUpdateListener, MenuItemUpdateListener{
	
	private Window m_window;
    private TopologyComponent m_topologyComponent;
    private Tree m_tree;
    private SimpleGraphContainer m_graphContainer;
    private CommandManager m_commandManager;
    private MenuBar m_menuBar;
    private AbsoluteLayout m_layout;
    
    public TopologyWidgetTestApplication(CommandManager commandManager, TopologyProvider topologyProvider) {
        m_commandManager = commandManager;
        m_commandManager.addMenuItemUpdateListener(this);
        m_graphContainer = new SimpleGraphContainer(topologyProvider);
    }
    
    
    @Override
    public void init() {
       
        m_layout = new AbsoluteLayout();
        m_layout.setSizeFull();
        
        m_window = new Window("Topology Widget Test");
        m_window.setContent(m_layout);
        setMainWindow(m_window);
        
        Refresher refresher = new Refresher();
        refresher.setRefreshInterval(5000);
        getMainWindow().addComponent(refresher);
        
        m_graphContainer.setLayoutAlgorithm(new KKLayoutAlgorithm());
        
        m_topologyComponent = new TopologyComponent(m_graphContainer);
        m_topologyComponent.setSizeFull();
        m_topologyComponent.addMenuItemStateListener(this);
        
        final Property scale = m_graphContainer.getProperty(DisplayState.SCALE);
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
        final Property zoomLevel = m_graphContainer.getProperty(DisplayState.SEMANTIC_ZOOM_LEVEL);
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
        
        getMainWindow().addListener(new Window.CloseListener() {
            
            @Override
            public void windowClose(CloseEvent e) {
               if(e.getWindow().getName().equals("Auth Window")){
                   getMainWindow().executeJavaScript("document.getElementById(\"termFocusPanel\").focus();");
               }
                
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
        
        
        m_commandManager.addActionHandlers(m_topologyComponent, m_graphContainer, getMainWindow());
        m_commandManager.addCommandUpdateListener(this);
        
        
        menuBarUpdated(m_commandManager);
        m_layout.addComponent(bottomLayoutBar, "top: 23px; left: 0px; right:0px; bottom:0px;");
        
        
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
    
    
    public void updateMenuItems() {
        updateMenuItems(m_menuBar.getItems());
        m_menuBar.requestRepaint();
    }

    private void updateMenuItems(List<MenuItem> menuItems) {
        for(MenuItem menuItem : menuItems) {
            if(menuItem.hasChildren()) {
                updateMenuItems(menuItem.getChildren());
            }else {
                m_commandManager.updateMenuItem(menuItem, m_graphContainer, getMainWindow());
            }
        }
    }


    @Override
    public void menuBarUpdated(CommandManager commandManager) {
        if(m_menuBar != null) {
            m_layout.removeComponent(m_menuBar);
        }
        
        m_menuBar = commandManager.getMenuBar(m_graphContainer, getMainWindow());
        m_menuBar.setWidth("100%");
        m_layout.addComponent(m_menuBar, "top: 0px; left: 0px; right:0px;");
        updateMenuItems();
    }

}
